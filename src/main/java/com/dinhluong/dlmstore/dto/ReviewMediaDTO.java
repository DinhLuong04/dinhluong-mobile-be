package com.dinhluong.dlmstore.dto;

import lombok.*;

@Data
    @Builder
public class ReviewMediaDTO {
    private Long id;
        private String image_url;
        private Boolean is_video;
}
