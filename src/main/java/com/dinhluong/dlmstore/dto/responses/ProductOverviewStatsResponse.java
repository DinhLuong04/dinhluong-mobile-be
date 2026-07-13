package com.dinhluong.dlmstore.dto.responses;

import lombok.*;

@Builder
@Data
public class ProductOverviewStatsResponse {
    private long totalProducts;
    private long activeProducts;
    private long inactiveProducts;
    private long outOfStockVariants; // Số biến thể (màu/dung lượng) hết hàng
    private long lowStockVariants;
}
