package com.dinhluong.dlmstore.entity;

import com.dinhluong.dlmstore.entity.imp.BaseEntity;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "categories")
@Data

public class Category  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;

}