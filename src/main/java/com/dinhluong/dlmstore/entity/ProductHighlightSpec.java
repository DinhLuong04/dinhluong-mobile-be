package com.dinhluong.dlmstore.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_highlight_specs")
@Data
public class ProductHighlightSpec {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @ToString.Exclude
    private Product product;

    private String label;

    private String value;

    @Column(name = "icon_url")
    private String iconUrl;
}