package com.dinhluong.dlmstore.entity;

import java.math.BigDecimal;
import java.util.List;
import com.dinhluong.dlmstore.convert.IntegerListConverter;
import com.dinhluong.dlmstore.convert.JsonNodeConverter;
import com.dinhluong.dlmstore.convert.StringListConverter;
import com.dinhluong.dlmstore.entity.Enums.OsType;
import com.dinhluong.dlmstore.entity.Enums.ProductStatus;
import com.dinhluong.dlmstore.entity.Enums.ProductType;
import com.dinhluong.dlmstore.entity.imp.BaseEntity;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Data
@EqualsAndHashCode(callSuper = true)
public class Product extends BaseEntity {

    private String name;
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "display_price")
    private BigDecimal displayPrice;

    @Column(name = "original_price")
    private BigDecimal originalPrice;

    @Column(name = "installment_text")
    private String installmentText;

    @Column(name = "highlight_features", columnDefinition = "TEXT")
    private String highlightFeatures;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type")
    private ProductType productType;

    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    // --- CÁC CỘT LỌC VẬT LÝ (INDEXED COLUMNS) ---

    @Enumerated(EnumType.STRING)
    @Column(name = "os_type")
    private OsType osType;

    @Column(name = "screen_size")
    private Double screenSize;

    @Column(name = "screen_resolution_type")
    private String screenResolutionType;

    @Column(name = "refresh_rate")
    private Integer refreshRate;

    @Column(name = "battery_capacity")
    private Integer batteryCapacity;

    @Column(name = "support_5g")
    private Boolean support5g;
   

    @Column(name = "special_features")
    private String specialFeatures;

    @Column(name = "search_keywords", columnDefinition = "TEXT")
    private String searchKeywords;

    // --- CÁC CỘT JSON (Yêu cầu Hibernate 6 + Jackson) ---
    
    @Convert(converter = IntegerListConverter.class)
    @Column(name = "available_rams", columnDefinition = "json")
    private List<Integer> availableRams; // VD: [4, 8, 12]

    @Convert(converter = StringListConverter.class)
    @Column(name = "available_roms", columnDefinition = "json")
    private List<String> availableRoms;// VD: ["128GB", "256GB"] hoặc [128, 256]

    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "specifications_json", columnDefinition = "json")
    private JsonNode specificationsJson;// Cache hiển thị chi tiết (Map cấu trúc tự do)

    // --- QUAN HỆ (RELATIONSHIPS) ---

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<ProductImage> images;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<ProductVariant> variants;

    // [THAY ĐỔI] Thay thế specGroups cũ bằng product_spec_values mới
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<ProductSpecValue> specValues;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<ProductHighlightSpec> highlightSpecs;
}