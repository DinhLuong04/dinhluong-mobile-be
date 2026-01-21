package com.dinhluong.dlmstore.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_spec_values")
@Data

public class ProductSpecValue  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER) // Eager để khi get Product lấy luôn tên thuộc tính
    @JoinColumn(name = "attribute_id", nullable = false)
    private SpecAttribute attribute;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String value;
}