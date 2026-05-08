package com.dinhluong.dlmstore.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_combos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductCombo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_product_id", nullable = false)
    private Product mainProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_product_id", nullable = false)
    private Product relatedProduct;

    @Column(name = "discount_amount")
    private BigDecimal discountAmount;

    @Column(name = "note")
    private String note;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
