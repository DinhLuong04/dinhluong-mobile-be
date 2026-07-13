package com.dinhluong.dlmstore.service.tools;

import com.dinhluong.dlmstore.entity.Product;
import com.dinhluong.dlmstore.entity.ProductSpecValue;
import com.dinhluong.dlmstore.service.ProductService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductDataEnricher {

    private final ObjectMapper objectMapper;
    private final ProductService productService;

    // =========================================================
    // MAIN METHOD
    // =========================================================

    /**
     * Rebuild specs json + keywords + stock
     * Chỉ dùng dữ liệu từ DB
     */
    public void enrichProductBeforeSave(Product product) {
        enrichProductBeforeSave(product, null);
    }

    /**
     * Rebuild specs json + keywords + stock
     * Có merge thêm custom json từ frontend
     */
    public void enrichProductBeforeSave(
            Product product,
            JsonNode customJsonFromClient
    ) {

        // Build specifications json
        ArrayNode specsJson = buildSpecificationsJson(
                product,
                customJsonFromClient
        );

        product.setSpecificationsJson(specsJson);

        // Build search keywords
        product.setSearchKeywords(
                buildSearchKeywords(product)
        );

        // Update stock
        updateTotalStock(product);
    }

    // =========================================================
    // BUILD SPECIFICATIONS JSON
    // =========================================================

    public ArrayNode buildSpecificationsJson(Product product) {
        return buildSpecificationsJson(product, null);
    }

    public ArrayNode buildSpecificationsJson(
            Product product,
            JsonNode customJsonFromClient
    ) {

        // GỘP GROUP TRÙNG TÊN
        Map<String, ArrayNode> groupMap = new LinkedHashMap<>();

        // =====================================================
        // 1. DỮ LIỆU EAV
        // =====================================================

        // =====================================================
        // 1. DỮ LIỆU EAV
        // =====================================================

        if (product.getSpecValues() != null && !product.getSpecValues().isEmpty()) {

            // BƯỚC 1: Lọc bỏ các giá trị bị thiếu Attribute hoặc Group để chống NullPointerException
            List<ProductSpecValue> validValues = product.getSpecValues().stream()
                    .filter(v -> v.getAttribute() != null && v.getAttribute().getGroup() != null)
                    .collect(Collectors.toList());

            // BƯỚC 2: Mới bắt đầu Sort trên danh sách đã an toàn
            validValues.sort(
                    Comparator
                            .comparing(
                                    (ProductSpecValue v) ->
                                            v.getAttribute().getGroup().getSortOrder() != null
                                                    ? v.getAttribute().getGroup().getSortOrder()
                                                    : 999
                            )
                            .thenComparing(
                                    v ->
                                            v.getAttribute().getSortOrder() != null
                                                    ? v.getAttribute().getSortOrder()
                                                    : 999
                            )
            );

            // BƯỚC 3: Xử lý dữ liệu
            for (ProductSpecValue specValue : validValues) {
                // Lúc này không cần check if (specValue.getAttribute() == null ...) nữa vì đã lọc ở trên rồi

                String title = specValue.getAttribute().getGroup().getName();

                ArrayNode itemsArray = groupMap.computeIfAbsent(
                        title,
                        k -> objectMapper.createArrayNode()
                );

                ObjectNode itemNode = objectMapper.createObjectNode();
                itemNode.put("label", specValue.getAttribute().getName());
                itemNode.put("value", specValue.getValue());

                itemsArray.add(itemNode);
            }
        }

        // =====================================================
        // 2. CUSTOM JSON
        // =====================================================

        if (customJsonFromClient != null
                && customJsonFromClient.isArray()) {

            for (JsonNode customGroup : customJsonFromClient) {

                if (!customGroup.has("title")
                        || !customGroup.has("items")) {
                    continue;
                }

                String title =
                        customGroup.get("title").asText();

                JsonNode customItems =
                        customGroup.get("items");

                ArrayNode itemsArray =
                        groupMap.computeIfAbsent(
                                title,
                                k -> objectMapper.createArrayNode()
                        );

                if (customItems.isArray()) {

                    for (JsonNode item : customItems) {
                        itemsArray.add(item);
                    }
                }
            }
        }

        // =====================================================
        // 3. FINAL JSON
        // =====================================================

        ArrayNode finalSpecsArray =
                objectMapper.createArrayNode();

        int idCounter = 1;

        for (Map.Entry<String, ArrayNode> entry
                : groupMap.entrySet()) {

            ObjectNode groupNode =
                    objectMapper.createObjectNode();

            groupNode.put("id", idCounter++);
            groupNode.put("title", entry.getKey());
            groupNode.set("items", entry.getValue());

            finalSpecsArray.add(groupNode);
        }

        return finalSpecsArray;
    }

    // =========================================================
    // BUILD SEARCH KEYWORDS
    // =========================================================

    public String buildSearchKeywords(Product product) {
        return productService.generateSearchKeywords(product);
    }

    // =========================================================
    // UPDATE STOCK
    // =========================================================

    public void updateTotalStock(Product product) {

        if (product.getVariants() == null) {
            product.setTotalStock(0);
            return;
        }

        int totalStock = product.getVariants()
                .stream()
                .mapToInt(v ->
                        v.getStockQuantity() != null
                                ? v.getStockQuantity()
                                : 0
                )
                .sum();

        product.setTotalStock(totalStock);
    }

    // =========================================================
    // REBUILD ONLY SPECS JSON
    // =========================================================

    public void rebuildSpecificationsJson(Product product) {

        product.setSpecificationsJson(
                buildSpecificationsJson(product)
        );
    }

    // =========================================================
    // REBUILD ONLY KEYWORDS
    // =========================================================

    public void rebuildSearchKeywords(Product product) {

        product.setSearchKeywords(
                buildSearchKeywords(product)
        );
    }

    // =========================================================
    // REBUILD ONLY STOCK
    // =========================================================

    public void rebuildStock(Product product) {
        updateTotalStock(product);
    }
}