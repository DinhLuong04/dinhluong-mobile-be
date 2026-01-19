package com.dinhluong.dlmstore.entity;

import java.math.BigDecimal;
import com.dinhluong.dlmstore.entity.imp.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_variants")
@Data

public class ProductVariant  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @ToString.Exclude
    private Product product;

    private String sku;
    
    private String rom;
    
    private String ram;

    @Column(name = "color_name") // <--- Đã sửa
    private String colorName;

    @Column(name = "color_hex") // <--- Đã sửa
    private String colorHex;

    private BigDecimal price;

    @Column(name = "stock_quantity") // <--- Đã sửa
    private Integer stockQuantity;

    @Column(name = "image_url") // <--- Đã sửa
    private String imageUrl;

    @Column(name = "is_active") // <--- Đã sửa
    private boolean isActive = true;
}