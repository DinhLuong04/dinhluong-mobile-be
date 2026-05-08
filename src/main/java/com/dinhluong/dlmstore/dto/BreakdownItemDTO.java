package com.dinhluong.dlmstore.dto;

import lombok.*;

@Data
@Builder
public class BreakdownItemDTO {
    private Integer star;
    private Double percent;
    private Long count;
}
