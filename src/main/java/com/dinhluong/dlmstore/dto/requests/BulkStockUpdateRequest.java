package com.dinhluong.dlmstore.dto.requests;

import lombok.Data;
import java.util.List;

@Data
public class BulkStockUpdateRequest {
    private Long productId; // ID của sản phẩm cha để tính lại tổng kho 1 lần
    private List<StockItem> stocks;

    @Data
    public static class StockItem {
        private Long variantId;
        private Integer stockQuantity;
    }
}