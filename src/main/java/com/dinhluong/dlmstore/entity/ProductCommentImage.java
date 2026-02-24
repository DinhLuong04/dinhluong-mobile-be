package com.dinhluong.dlmstore.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
@Entity
@Table(name = "product_comment_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCommentImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private ProductComment comment;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_video")
    private Boolean isVideo;
}