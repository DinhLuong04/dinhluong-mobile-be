package com.dinhluong.dlmstore.entity;

import com.dinhluong.dlmstore.entity.imp.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String slug;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    // Quản lý danh mục cha - con (Tùy chọn, dùng để phân cấp)
    @Column(name = "parent_id")
    private Long parentId;

    private Integer level;
}