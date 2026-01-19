package com.dinhluong.dlmstore.entity;
import java.math.BigDecimal;

import com.dinhluong.dlmstore.entity.imp.BaseEntity;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "brands")
@Data
public class Brand  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
}
