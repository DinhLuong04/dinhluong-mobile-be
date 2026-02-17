package com.dinhluong.dlmstore.entity;


import com.dinhluong.dlmstore.entity.imp.BaseEntity;
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
public class ProductCombo  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // Sản phẩm chính (Ví dụ: iPhone 15)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_product_id", nullable = false)
    private Product mainProduct;

    // Sản phẩm mua kèm (Ví dụ: Ốp lưng)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_product_id", nullable = false)
    private Product relatedProduct;

    @Column(name = "discount_amount")
    private BigDecimal discountAmount; // Số tiền giảm (VD: 50000)

    @Column(name = "note")
    private String note; // Ghi chú (VD: "Tiết kiệm 50k")
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // KHÔNG khai báo updatedAt
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
