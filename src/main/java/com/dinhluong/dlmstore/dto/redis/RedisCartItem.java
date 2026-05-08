package com.dinhluong.dlmstore.dto.redis;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedisCartItem {
    private Long productVariantId;
    private Integer quantity;
    private List<Long> comboVariantIds;
}