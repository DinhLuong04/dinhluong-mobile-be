package com.dinhluong.dlmstore.entity;

import java.math.BigDecimal;
import java.util.List;

import com.dinhluong.dlmstore.entity.Enums.OsType;
import com.dinhluong.dlmstore.entity.Enums.ProductStatus;
import com.dinhluong.dlmstore.entity.Enums.ProductType;
import com.dinhluong.dlmstore.entity.imp.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Data
@EqualsAndHashCode(callSuper = true)
public class Product extends BaseEntity {
    
    private String name; // Tên cột DB là 'name' giống nhau nên không cần @Column
    private String slug; // Tên cột DB là 'slug' giống nhau
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "display_price") // Ánh xạ sang display_price
    private BigDecimal displayPrice;

    @Column(name = "original_price") // Ánh xạ sang original_price
    private BigDecimal originalPrice;

    @Column(name = "installment_text") // Ánh xạ sang installment_text
    private String installmentText;
    
    @Column(name = "highlight_features", columnDefinition = "TEXT") // Ánh xạ sang highlight_features
    private String highlightFeatures; 

    @ManyToOne
    @JoinColumn(name = "brand_id") // Đã có, giữ nguyên
    private Brand brand;

    @ManyToOne
    @JoinColumn(name = "category_id") // Đã có, giữ nguyên
    private Category category;

    @Column(name = "thumbnail_url") // Ánh xạ sang thumbnail_url
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type") // Ánh xạ sang product_type
    private ProductType productType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status") // Tên giống nhau, nhưng thêm cho rõ ràng cũng được
    private ProductStatus status;

    // --- Các cột Filter nhanh (Từ lệnh ALTER TABLE) ---
    
    @Enumerated(EnumType.STRING)
    @Column(name = "os_type") // Sửa lỗi quan trọng ở đây
    private OsType osType;

    @Column(name = "screen_size") // Sửa lỗi
    private Double screenSize;

    @Column(name = "screen_resolution_type") // Sửa lỗi
    private String screenResolutionType;

    @Column(name = "refresh_rate") // Sửa lỗi
    private Integer refreshRate;

    @Column(name = "battery_capacity") // Sửa lỗi chính bạn đang gặp
    private Integer batteryCapacity;

    @Column(name = "support_5g") // Sửa lỗi
    private Boolean support5g;

    @Column(name = "special_features") // Sửa lỗi
    private String specialFeatures;
    
    @Column(name = "search_keywords", columnDefinition = "TEXT") // Sửa lỗi
    private String searchKeywords;

    // --- Quan hệ thành phần (Composition) ---
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<ProductImage> images;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<ProductVariant> variants;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<ProductSpecGroup> specGroups;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<ProductHighlightSpec> highlightSpecs;
}