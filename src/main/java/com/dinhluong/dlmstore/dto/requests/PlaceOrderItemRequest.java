package com.dinhluong.dlmstore.dto.requests;

import java.util.List;

import lombok.Data;

@Data
public class PlaceOrderItemRequest {
    private Long variantId;
    private Integer quantity; // Số lượng của sản phẩm chính
    private List<Long> comboIds;
}