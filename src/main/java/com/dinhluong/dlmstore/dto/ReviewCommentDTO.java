package com.dinhluong.dlmstore.dto;

import java.util.List;

import lombok.*;

@Data
@Builder
public class ReviewCommentDTO {
    private Long id;
    private String author_name;
    private String author_avatar;
    private Integer rating;
    private String content;
    private String created_at;
    private Boolean is_mine;
    private Boolean is_purchased;
    private List<ReviewMediaDTO> images;
    private List<ReviewReplyDTO> replies;
}
