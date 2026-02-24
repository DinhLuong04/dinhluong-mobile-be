package com.dinhluong.dlmstore.dto;

import lombok.*;

@Data
    @Builder
public class ReviewReplyDTO {
    private Long id;
        private String author_name;
        private String author_avatar;
        private Boolean is_admin_reply;
        private String content;
        private String created_at;
        private Boolean is_mine;
}
