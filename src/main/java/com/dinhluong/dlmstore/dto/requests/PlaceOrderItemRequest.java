package com.dinhluong.dlmstore.dto.requests;

import java.util.List;

import lombok.Data;

@Data
public class PlaceOrderItemRequest {
    private Long variantId;
    private Integer quantity;
    private List<Long> comboIds;
}