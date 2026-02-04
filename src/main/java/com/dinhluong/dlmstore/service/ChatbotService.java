package com.dinhluong.dlmstore.service;

import com.dinhluong.dlmstore.utils.GeminiClient;
import com.dinhluong.dlmstore.dto.ChatProductDto;
import com.dinhluong.dlmstore.dto.GeminiFilterCriteria;
import com.dinhluong.dlmstore.dto.ProductAiDto;
import com.dinhluong.dlmstore.dto.responses.AiFilterResponse;
import com.dinhluong.dlmstore.dto.responses.ChatBotResponse;
import com.dinhluong.dlmstore.dto.responses.ProductCardResponse;
import com.dinhluong.dlmstore.entity.Product;
import com.dinhluong.dlmstore.entity.ProductVariant;
import com.dinhluong.dlmstore.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final GeminiClient geminiClient;
    private final ProductService productService; 
    private final ProductRepository productRepository; 
    private final ObjectMapper objectMapper;

    public ChatBotResponse processUserMessage(String userMessage) {
        //  Phân tích Intent
        GeminiFilterCriteria criteria = extractIntentAndFilter(userMessage);

        if (criteria == null)
            return new ChatBotResponse("Hệ thống đang bận.", new ArrayList<>());

        if ("CHAT".equalsIgnoreCase(criteria.getIntent())) {
            String chatReply = geminiClient.callGemini("Trả lời ngắn gọn, vui vẻ: " + userMessage);
            return new ChatBotResponse(chatReply, new ArrayList<>());
        }

        List<ProductAiDto> contextForAi = new ArrayList<>();
        List<ChatProductDto> fullListUi = new ArrayList<>();

        try {
            Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "displayPrice"));
            List<ProductCardResponse> rawProductList = new ArrayList<>(); 

            // CASE 1: DETAIL (Chi tiết 1 sản phẩm)
            if ("DETAIL".equalsIgnoreCase(criteria.getIntent())) {
                String keyword = criteria.getTargetProductName();
                Page<ProductCardResponse> page = productService.getAllProducts(
                        null, null, null, null, null, null, null, null, null, null, null, null, null,
                        keyword, pageable);
                if (page.hasContent())
                    rawProductList.addAll(page.getContent());
            }

            // CASE 2: COMPARE (So sánh nhiều sản phẩm) 
            else if ("COMPARE".equalsIgnoreCase(criteria.getIntent()) && criteria.getTargetProductName() != null) {
                String[] models = criteria.getTargetProductName().split(",");

                for (String modelName : models) {
                    String cleanName = modelName.trim(); 
                    if (!cleanName.isEmpty()) {
                        Page<ProductCardResponse> p = productService.getAllProducts(
                                null, null, null, null, null, null, null, null, null, null, null, null, null,
                                cleanName, pageable);
                        if (p.hasContent()) {
                            rawProductList.add(p.getContent().get(0));
                        }
                    }
                }
            }

            // CASE 3: SEARCH / CHAT (Tìm kiếm chung theo bộ lọc)
            else {
                Page<ProductCardResponse> page = productService.getAllProducts(
                        criteria.getBrands(), criteria.getOsTypes(), null, null,
                        criteria.getNetworks(), criteria.getMinPrice(), criteria.getMaxPrice(),
                        null, null, null, null, null, null, criteria.getSearchKeyword(), pageable);
                if (page.hasContent())
                    rawProductList.addAll(page.getContent());
            }
            if (!rawProductList.isEmpty()) {
                for (ProductCardResponse card : rawProductList) {
                    if (contextForAi.stream().anyMatch(p -> p.getId().equals(card.getId())))
                        continue;

                    productRepository.findById(card.getId()).ifPresent(p -> {
                        contextForAi.add(toAiDto(p));

                        // tính giá giảm 
                        String discountLabel = null;
                        if (p.getOriginalPrice() != null && p.getOriginalPrice().compareTo(p.getDisplayPrice()) > 0) {
                            BigDecimal diff = p.getOriginalPrice().subtract(p.getDisplayPrice());
                            discountLabel = "-" + String.format("%,.0f", diff) + "đ";
                        }

                        fullListUi.add(ChatProductDto.builder()
                                .id(p.getId())
                                .name(p.getName())
                                .slug(p.getSlug())
                                .image(p.getThumbnailUrl())
                                .price(p.getDisplayPrice())
                                .originalPrice(p.getOriginalPrice())
                                .discountLabel(discountLabel)
                                .configSummary(p.getVariants().isEmpty() ? "" : p.getVariants().get(0).getRom())
                                .build());
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // GỌI AI: Truyền thêm Intent để chọn Style trả lời
        AiFilterResponse aiResult = generateSmartResponse(userMessage, contextForAi, criteria.getIntent());

        //  LỌC DANH SÁCH UI
        List<ChatProductDto> finalUiList = new ArrayList<>();
        if (aiResult.getProductIds() != null && !aiResult.getProductIds().isEmpty()) {
            for (ChatProductDto item : fullListUi) {
                if (aiResult.getProductIds().contains(item.getId())) {
                    finalUiList.add(item);
                }
            }
        } else {
            if (!fullListUi.isEmpty())
                finalUiList.add(fullListUi.get(0));
        }

        // Giới hạn hiển thị UI tùy theo intent (Search hiện nhiều, Detail hiện ít)
        int limit = "SEARCH".equalsIgnoreCase(criteria.getIntent()) ? 5 : 2;
        if (finalUiList.size() > limit)
            finalUiList = finalUiList.subList(0, limit);

        return ChatBotResponse.builder()
                .answer(aiResult.getAnswer())
                .products(finalUiList)
                .build();
    }

   
    private AiFilterResponse generateSmartResponse(String userMessage, List<ProductAiDto> products, String intent) {
        if (products.isEmpty())
            return new AiFilterResponse("Dạ em chưa tìm thấy sản phẩm nào khớp ý ạ.", new ArrayList<>());

        try {
            String jsonContext = objectMapper.writeValueAsString(products);

            //  Xác định style trả lời dựa trên Intent
            String styleInstruction;

            if ("SEARCH".equalsIgnoreCase(intent)) {
                // Style cho SEARCH: Ngắn gọn, súc tích
                styleInstruction = """
                        - Đây là dạng câu hỏi TÌM KIẾM DANH SÁCH.
                        - Yêu cầu: Trả lời NGẮN GỌN (tối đa 3 dòng).
                        - Chỉ cần nói tổng quan: "Dạ, dưới đây là top [số lượng] sản phẩm [tiêu chí] tốt nhất...".
                        - KHÔNG liệt kê cấu hình chi tiết (vì sẽ hiện thẻ sản phẩm).
                        - Văn phong nhanh gọn, hướng khách click vào thẻ.
                        """;
            } else if ("DETAIL".equalsIgnoreCase(intent)) {
                styleInstruction = """
                        - Đây là yêu cầu TƯ VẤN CHI TIẾT một sản phẩm.
                        - Nhiệm vụ: Hãy đóng vai chuyên gia, phân tích cực sâu các thông số trong dữ liệu JSON (Chip, RAM, Camera, Pin).
                        - Nêu bật lợi ích: Máy này phù hợp để chơi game gì? Chụp ảnh thế nào? Pin dùng được bao lâu?
                        - Tình trạng kho: Nhắc đến các màu sắc đang còn hàng.
                        - Khuyến mãi: Nhấn mạnh các ưu đãi đang có.
                        - Văn phong: Nhiệt tình, chuyên nghiệp, dùng nhiều icon 🔥✨🚀.
                        """;
            } else {
                styleInstruction = """
                        - Đây là câu hỏi SO SÁNH.
                        - Yêu cầu bắt buộc: **Tạo một Bảng so sánh Markdown (Markdown Table)**.
                        - Cột 1: Tiêu chí (Màn hình, Hiệu năng, Camera, Pin...).
                        - Các cột sau: Tên sản phẩm.
                        - Trong ô dữ liệu:
                          + Viết thông số ngắn gọn.
                          + Dùng icon ✅ hoặc 🏆 vào bên nào TỐT HƠN.
                        - Sau bảng, viết một câu kết luận ngắn gọn: "Nên mua máy nào nếu...".
                        """;
            }

            //Ghép vào Prompt tổng
            String prompt = """
                    Bạn là tư vấn viên AI của DLM Store.
                    Khách hỏi: "%s"
                    Dữ liệu JSON: ```json %s ```

                    NHIỆM VỤ:
                    1. Lọc rác: Chọn ra các ID sản phẩm phù hợp nhất.
                    2. Viết lời tư vấn theo phong cách sau:
                    %s

                    OUTPUT JSON:
                    {
                        "answer": "Nội dung text...",
                        "productIds": [1, 2, 3]
                    }
                    """.formatted(userMessage, jsonContext, styleInstruction);

            String raw = geminiClient.callGemini(prompt);
            return parseJsonResult(raw, AiFilterResponse.class);

        } catch (Exception e) {
            return new AiFilterResponse("Dạ mời bạn tham khảo danh sách bên dưới ạ:", new ArrayList<>());
        }
    }

    // --- HELPER MAPPERS ---
    private ProductAiDto toAiDto(Product product) {
        List<String> inventory = new ArrayList<>();
        if (product.getVariants() != null) {
            product.getVariants().forEach(v -> inventory
                    .add(v.getColorName() + "-" + v.getRom() + ": " + (v.getStockQuantity() > 0 ? "Còn" : "Hết")));
        }

        Map<String, String> specs = new HashMap<>();
        if (product.getHighlightSpecs() != null) {
            product.getHighlightSpecs().forEach(h -> specs.put(h.getLabel(), h.getValue()));
        }

        StringBuilder promo = new StringBuilder();
        if (product.getOriginalPrice() != null && product.getDisplayPrice() != null
                && product.getOriginalPrice().compareTo(product.getDisplayPrice()) > 0) {
            promo.append("Giảm ").append(product.getOriginalPrice().subtract(product.getDisplayPrice())).append("đ; ");
        }
        if (StringUtils.hasText(product.getInstallmentText()))
            promo.append(product.getInstallmentText());

        return ProductAiDto.builder()
                .id(product.getId()) 
                .name(product.getName())
                .price(product.getDisplayPrice())
                .inventory(inventory)
                .specs(specs)
                .promotion(promo.toString())
                .build();
    }

    private GeminiFilterCriteria extractIntentAndFilter(String userMessage) {
        String prompt = """
                Bạn là chuyên gia phân tích yêu cầu khách hàng cho cửa hàng điện thoại.
                Câu hỏi của khách: "%s"

                NHIỆM VỤ: Xác định Intent và Filter dựa trên quy tắc sau:

                1. PHÂN LOẠI INTENT:
                   - "DETAIL": Khi khách hỏi về MỘT sản phẩm cụ thể hoặc yêu cầu "tư vấn", "đánh giá", "cấu hình" của một máy đích danh. (VD: "Tư vấn máy Xiaomi 15T", "Cấu hình iPhone 15").
                   - "SEARCH": Khi khách hỏi tìm một nhóm máy hoặc tìm theo tiêu chí chung chung. (VD: "Tìm máy Xiaomi tầm 10tr", "Điện thoại pin trâu").
                   - "COMPARE": Khi khách nhắc đến 2 sản phẩm trở lên để so sánh.
                   - "CHAT": Chào hỏi, xã giao.

                2. TRÍCH XUẤT FILTER:
                   - targetProductName: Tên máy cụ thể (Nếu intent là DETAIL hoặc COMPARE).
                   - searchKeyword: Từ khóa tìm kiếm chung.
                   - brands: ["Xiaomi", "Apple", "Samsung"...]
                   - minPrice / maxPrice: Khoảng giá khách tìm.

                OUTPUT JSON FORMAT ONLY:
                {
                  "intent": "DETAIL" | "SEARCH" | "COMPARE" | "CHAT",
                  "targetProductName": "Xiaomi 15T 5G",
                  "searchKeyword": null,
                  "brands": ["Xiaomi"],
                  "minPrice": null,
                  "maxPrice": null
                }
                """
                .formatted(userMessage);

        String raw = geminiClient.callGemini(prompt);
        System.out.println("gemini intent :\n" + raw);
        return parseJsonResult(raw, GeminiFilterCriteria.class);
    }

    private <T> T parseJsonResult(String raw, Class<T> clazz) {
        try {
            String json = raw.replaceAll("```json", "").replaceAll("```", "").trim();
            if (json.contains("{"))
                json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    private String getRomSummary(Product p) {
        if (p.getVariants() != null && !p.getVariants().isEmpty())
            return p.getVariants().get(0).getRom();
        return "";
    }
}
