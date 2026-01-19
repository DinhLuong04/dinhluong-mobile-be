package com.dinhluong.dlmstore.entity;

import java.util.List;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_spec_groups")
@Data
public class ProductSpecGroup {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @ToString.Exclude
    private Product product;

    private String title;

    @Column(name = "sort_order") // <--- Đã sửa
    private Integer sortOrder;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<ProductSpecItem> items;
}