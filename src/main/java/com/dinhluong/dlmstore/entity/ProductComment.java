package com.dinhluong.dlmstore.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "product_comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "author_name", length = 100)
    private String authorName;

    @Column(name = "author_phone", length = 20)
    private String authorPhone;

    @Column(name = "author_avatar", length = 500)
    private String authorAvatar;

    @Column(name = "rating")
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "is_admin_reply")
    private Boolean isAdminReply;

    @Column(name = "is_purchased")
    private Boolean isPurchased;

    @Enumerated(EnumType.STRING)
    private CommentStatus status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null)
            this.status = CommentStatus.PENDING;
    }

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductCommentImage> images;

    public enum CommentStatus {
        PENDING, APPROVED, REJECTED
    }
}
