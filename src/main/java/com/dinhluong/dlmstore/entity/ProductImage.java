package com.dinhluong.dlmstore.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_images")
@Data
public class ProductImage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @ToString.Exclude
    private Product product;

    @Column(name = "image_url") // <--- Đã sửa
    private String imageUrl;

    @Column(name = "sort_order") // <--- Đã sửa
    private Integer sortOrder;
}