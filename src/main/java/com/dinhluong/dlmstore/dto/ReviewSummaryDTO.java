package com.dinhluong.dlmstore.dto;

import java.util.List;

import lombok.*;

@Data
    @Builder
public class ReviewSummaryDTO {
    private Double average;
        private Long totalCount;
        private List<BreakdownItemDTO> breakdown;
        private boolean currentUserHasPurchased;
}
