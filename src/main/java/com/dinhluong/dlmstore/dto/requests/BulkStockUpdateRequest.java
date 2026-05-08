package com.dinhluong.dlmstore.dto.requests;

import lombok.Data;
import java.util.List;

@Data
public class BulkStockUpdateRequest {
    private Long productId;
    private List<StockItem> stocks;

    @Data
    public static class StockItem {
        private Long variantId;
        private Integer stockQuantity;
    }
}