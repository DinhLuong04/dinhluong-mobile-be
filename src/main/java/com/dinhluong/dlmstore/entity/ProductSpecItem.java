package com.dinhluong.dlmstore.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_spec_items")
@Data
public class ProductSpecItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    @ToString.Exclude
    private ProductSpecGroup group;

    private String label;

    @Column(columnDefinition = "TEXT") // <--- Thêm cái này cho an toàn
    private String value;
}