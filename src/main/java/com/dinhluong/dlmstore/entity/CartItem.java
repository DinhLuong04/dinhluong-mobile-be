package com.dinhluong.dlmstore.entity;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "cart_items")
@Data
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cart_id", nullable = false)
    private Long cartId;

    @Column(name = "product_variant_id", nullable = false)
    private Long productVariantId;

    @Column(nullable = false)
    private Integer quantity;

    // Chìa khóa để gom nhóm Combo: Nếu null -> SP chính. Nếu có số -> Phụ kiện mua kèm của SP chính đó
    @Column(name = "parent_id")
    private Long parentId; 
}