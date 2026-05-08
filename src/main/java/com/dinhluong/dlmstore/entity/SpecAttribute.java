package com.dinhluong.dlmstore.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "spec_attributes")
@Data
public class SpecAttribute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "data_type")
    private String dataType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private SpecGroup group;
}