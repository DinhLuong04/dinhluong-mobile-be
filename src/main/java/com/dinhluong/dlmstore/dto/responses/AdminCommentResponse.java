package com.dinhluong.dlmstore.dto.responses;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminCommentResponse {
    private Long id;
    private Long productId;
    private Long userId;
    private String productName;
    private String productThumbnail;
    private String productSlug;
    private String authorName;
    private String authorPhone;
    private String authorAvatar;
    private Integer rating;
    private String content;
    private Boolean isPurchased;
    private Boolean isAdminReply;
    private String status;
    private LocalDateTime createdAt;
    private List<String> imageUrls;
    private List<AdminCommentResponse> replies;
}