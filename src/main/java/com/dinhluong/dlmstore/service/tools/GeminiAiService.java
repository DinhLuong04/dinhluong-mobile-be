package com.dinhluong.dlmstore.service.tools;

import com.dinhluong.dlmstore.dto.requests.AiAccessoryRequest;
import com.dinhluong.dlmstore.dto.requests.AiContentRequest;
import com.dinhluong.dlmstore.dto.requests.AiSpecExtractRequest;
import com.dinhluong.dlmstore.dto.responses.AiBusinessInsightResponse;
import com.dinhluong.dlmstore.dto.responses.DashboardResponse;
import com.dinhluong.dlmstore.utils.GeminiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GeminiAiService {

    private final ObjectMapper objectMapper;
    
    // Tiêm GeminiClient đã có sẵn cơ chế tự động đổi Key khi sập
    private final GeminiClient geminiClient;

    public AiBusinessInsightResponse analyzeDashboardData(DashboardResponse dashboardData) {
        try {
            // 1. Chuyển dữ liệu Dashboard thành JSON Text
            String jsonData = objectMapper.writeValueAsString(dashboardData);

            // 2. Xây dựng Prompt (Câu lệnh cho AI)
            String prompt = """
            Bạn là Giám đốc Kinh doanh (CCO) cấp cao của hệ thống bán lẻ thiết bị di động DLMStore.

            Dưới đây là dữ liệu Dashboard (bao gồm doanh thu, sản phẩm, lý do hủy đơn, tồn kho, đánh giá hỗ trợ) trong kỳ vừa qua:

            %s

            Nhiệm vụ của bạn là phân tích chuyên sâu và BẮT BUỘC trả về kết quả dưới định dạng chuẩn JSON theo đúng cấu trúc sau.
            KHÔNG được dùng markdown.
            KHÔNG được dùng ```json
            Chỉ trả raw JSON hợp lệ.

            {
              "executiveSummary": "Tóm tắt ngắn gọn 2-3 câu về tình hình kinh doanh tổng thể",
              "financialInsight": {
                "trend": "UP hoặc DOWN hoặc STABLE",
                "analysis": "Phân tích doanh thu, tỷ lệ thanh toán"
              },
              "salesAndProduct": {
                "topPerformersAnalysis": "Nhận định sản phẩm",
                "crossSellOpportunities": "Đề xuất combo"
              },
              "customerService": {
                "satisfactionAnalysis": "Đánh giá CSKH",
                "cancellationInsights": "Phân tích hủy đơn"
              },
              "riskAlerts": [
                {
                  "severity": "HIGH",
                  "issue": "Tên rủi ro",
                  "recommendation": "Cách xử lý"
                }
              ],
              "actionableAdvices": [
                {
                  "title": "Tên hành động",
                  "detail": "Chi tiết",
                  "expectedImpact": "Kết quả"
                }
              ]
            }
            """.formatted(jsonData);

            // 3. Gọi Gemini thông qua GeminiClient
            // Nếu API Key 1 lỗi, nó sẽ tự động lấy Key 2, Key 3... để gọi lại
            String aiText = geminiClient.callGemini(prompt);

            // Nếu hệ thống trả về thông báo lỗi dạng Text (Không phải JSON)
            if (aiText.contains("Hệ thống đang bận") || aiText.contains("Xin lỗi, AI không phản hồi")) {
                throw new RuntimeException("AI đang quá tải, vui lòng thử lại sau.");
            }

            // 4. Dọn dẹp Text trả về để lấy ra JSON chuẩn
            aiText = aiText.replaceAll("```json", "").replaceAll("```", "").trim();
            if (aiText.contains("{")) {
                aiText = aiText.substring(aiText.indexOf("{"), aiText.lastIndexOf("}") + 1);
            }

            // 5. Map dữ liệu vào DTO
            return objectMapper.readValue(aiText, AiBusinessInsightResponse.class);

        } catch (Exception e) {
            throw new RuntimeException("Không thể phân tích AI: " + e.getMessage());
        }
    }

    public String generateProductDescription(AiContentRequest request) {
        // 1. Xây dựng Prompt ép AI chèn mã giữ chỗ [IMG_0], [IMG_1]...
        String prompt = String.format("""
           Bạn là chuyên gia viết nội dung SEO và tư vấn bán hàng cho cửa hàng điện thoại DLM Store.
            Hãy viết một bài đánh giá chi tiết, hấp dẫn bằng HTML cho sản phẩm: %s.
            Thông số kỹ thuật tham khảo: %s
            
            QUY TẮC CHÈN ẢNH (RẤT QUAN TRỌNG):
            - Sau thẻ <h2> mở đầu, hãy chèn thẻ giữ chỗ: [IMG_0]
            - Trong thân bài, sau mỗi mục <h3> quan trọng, hãy chèn lần lượt: [IMG_1], [IMG_2], [IMG_3]...
            - Chỉ chèn thẻ nếu đó là vị trí hợp lý để minh họa.
            
            YÊU CẦU NỘI DUNG VÀ ĐỊNH DẠNG (RẤT QUAN TRỌNG):
            - KHÔNG kẻ bảng thông số kỹ thuật (vì website đã có phần này riêng).
            - Không liệt kê thông số thô cứng. Hãy biến thông số thành lợi ích cho người dùng (Ví dụ: "Camera 48MP giúp chụp ảnh đêm sắc nét..."). 
            - Có thể dùng <ul> <li> để tóm tắt các điểm nhấn chính trong từng phần.
            - Trả về mã HTML thuần (tuyệt đối không dùng ký hiệu ```html).
            - TẤT CẢ các đoạn văn bản mô tả (text) TUYỆT ĐỐI BẮT BUỘC phải được bọc trong thẻ <p> và </p>. Không được để văn bản trần dính vào nhau.
            - Các thẻ hợp lệ được phép dùng: <h2>, <h3>, <p>, <ul>, <li>, <strong>, <em>.
            """, 
            request.getProductName(), 
            request.getSpecificationsJson()
        );

        // 2. Gọi Gemini lấy nội dung thô
        String rawHtml = geminiClient.callGemini(prompt);

        // 3. Logic "Bồi" ảnh thực tế vào các vị trí [IMG_x]
        return injectImages(rawHtml, request.getImageUrls(), request.getProductName());
    }

    public String generateAccessoryDescription(AiAccessoryRequest request) {
    // Prompt được tinh chỉnh riêng cho Phụ kiện
    String prompt = String.format("""
        Bạn là chuyên gia tư vấn phụ kiện công nghệ tại DLM Store.
        Hãy viết một bài giới thiệu chi tiết và thuyết phục khách hàng cho sản phẩm: %s.
        Dựa trên thông số kỹ thuật: %s
        
        QUY TẮC CHÈN ẢNH:
        - Chèn [IMG_0] ngay sau thẻ <h2> tiêu đề đầu tiên.
        - Chèn [IMG_1], [IMG_2]... sau mỗi mục <h3> mô tả tính năng quan trọng.
        
        YÊU CẦU NỘI DUNG (VĂN PHONG PHỤ KIỆN):
        - Nhấn mạnh vào: Độ bền vật liệu, Công nghệ an toàn (chống cháy nổ, ổn định dòng điện), và đặc biệt là TÍNH TƯƠNG THÍCH (Dùng cho máy nào, cổng gì).
        - Tuyệt đối bọc tất cả đoạn văn trong thẻ <p>.
        - Không kẻ bảng. Sử dụng <h3> cho tiêu đề mục và <ul><li> cho các điểm nhấn.
        - Trả về mã HTML thuần, không dùng ký hiệu ```html.
        """, 
        request.getProductName(), 
        request.getSpecificationsJson()
    );

    String rawHtml = geminiClient.callGemini(prompt);
    rawHtml = rawHtml.replaceAll("\\*\\*(.*?)\\*\\*", "<strong>$1</strong>");
    // Loại bỏ dấu sao thừa ở đầu dòng nếu có
    rawHtml = rawHtml.replaceAll("^\\* ", "");
    // Tận dụng lại hàm injectImages cũ của bạn vì logic thay thế [IMG_x] là giống nhau
    return injectImages(rawHtml, request.getImageUrls(), request.getProductName());
}





    private String injectImages(String html, List<String> imageUrls, String productName) {
        String finalHtml = html;

        // TRƯỜNG HỢP 1: NẾU CHƯA CÓ ẢNH (SỬA LẠI Ở ĐÂY)
        if (imageUrls == null || imageUrls.isEmpty()) {
            // Thay vì chèn ảnh demo, ta xóa sạch các thẻ giữ chỗ [IMG_x] để bài viết chỉ có chữ
            return finalHtml.replaceAll("\\[IMG_\\d+\\]", "");
        }

        // TRƯỜNG HỢP 2: ĐÃ CÓ ẢNH THẬT (Giữ nguyên logic của bạn)
        for (int i = 0; i < imageUrls.size(); i++) {
            String placeholder = "[IMG_" + i + "]";
            if (finalHtml.contains(placeholder)) {
                String imgHtml = String.format(
                        "<div class='pd-content-image'>" +
                                "  <img src='%s' alt='%s' />" +
                                "</div>",
                        imageUrls.get(i), productName
                );
                finalHtml = finalHtml.replace(placeholder, imgHtml);
            }
        }

        // Dọn dẹp các thẻ [IMG_x] thừa (ví dụ AI chèn 5 thẻ nhưng bạn chỉ đưa 3 ảnh)
        return finalHtml.replaceAll("\\[IMG_\\d+\\]", "");
    }

    public String extractSpecsFromText(AiSpecExtractRequest request) {
        String prompt = """
                Bạn là một công cụ AI chuyên trích xuất dữ liệu điện thoại di động có độ chính xác tuyệt đối.
                
                DANH SÁCH THUỘC TÍNH (Định dạng: ID - Tên thuộc tính):
                %s

                VĂN BẢN ĐẦU VÀO:
                "%s"

                NHIỆM VỤ:
                1. Đọc văn bản đầu vào, tìm các giá trị tương ứng với 'Tên thuộc tính' trong Danh sách.
                2. Nếu tìm thấy, hãy gán giá trị đó cho 'ID' tương ứng.
                3. Trả về chuẩn JSON định dạng Key-Value, trong đó Key là ID (chuỗi string), Value là giá trị tìm được (chỉ lấy thông số, ngắn gọn).
                4. Nếu thuộc tính nào KHÔNG có thông tin trong văn bản, BỎ QUA ID đó (Không đưa vào JSON).
                5. TUYỆT ĐỐI KHÔNG giải thích thêm. KHÔNG dùng markdown ```json. CHỈ TRẢ VỀ RAW JSON.

                VÍ DỤ OUTPUT BẮT BUỘC:
                {
                  "1": "6.7 inch",
                  "3": "5000 mAh",
                  "7": "Snapdragon 8 Gen 3"
                }
                """.formatted(request.getAttributesInfo(), request.getRawText());

        try {
            // Gọi Client Gemini của bạn
            String aiResult = geminiClient.callGemini(prompt);

            // Dọn rác Markdown nếu AI lỡ tay bọc JSON trong markdown
            if (aiResult.contains("```json")) {
                aiResult = aiResult.replace("```json", "");
                aiResult = aiResult.replace("```", "");
            } else if (aiResult.contains("```")) {
                aiResult = aiResult.replace("```", "");
            }

            return aiResult.trim();

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi AI bóc tách thông số: " + e.getMessage());
        }
    }

    public String extractAccessorySpecsJson(String rawText) {
    String prompt = String.format("""
        Bạn là công cụ AI chuyên bóc tách thông số phụ kiện công nghệ.
        Nhiệm vụ: Đọc văn bản thô và chuyển thành mảng JSON theo nhóm.
        
        VĂN BẢN ĐẦU VÀO:
        "%s"

        YÊU CẦU ĐỊNH DẠNG JSON (TRẢ VỀ RAW JSON):
        [
          {
            "id": 1,
            "title": "Thông số kỹ thuật",
            "items": [
              {"label": "Tên thuộc tính", "value": "Giá trị"}
            ]
          }
        ]
        
        LƯU Ý: 
        - id tự tăng từ 1. 
        - Tự nhóm các thuộc tính liên quan vào cùng một 'title' (Ví dụ: Thiết kế, Năng lượng, Kết nối).
        """, rawText);

    try {
        String aiResult = geminiClient.callGemini(prompt);
        // Dọn rác Markdown
        return aiResult.replaceAll("```json|```", "").trim();
    } catch (Exception e) {
        throw new RuntimeException("Lỗi AI bóc tách phụ kiện: " + e.getMessage());
    }
}
}