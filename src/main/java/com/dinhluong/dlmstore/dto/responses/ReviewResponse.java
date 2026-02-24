package com.dinhluong.dlmstore.dto.responses;

import lombok.Builder;
import lombok.Data;
import java.util.List;

import com.dinhluong.dlmstore.dto.ReviewCommentDTO;
import com.dinhluong.dlmstore.dto.ReviewSummaryDTO;

@Data
@Builder
public class ReviewResponse {
    private ReviewSummaryDTO summary;
    private List<ReviewCommentDTO> comments;
}
