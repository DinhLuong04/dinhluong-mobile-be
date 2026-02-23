package com.dinhluong.dlmstore.entity;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal discount;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false) 
    private DiscountType discountType;

    @Column(name = "min_order_amount", precision = 15, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "used_count")
    private Integer usedCount = 0;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    public enum DiscountType {
        PERCENT, FIXED
    }
    
    // Hàm helper kiểm tra xem đơn hàng có đủ điều kiện áp dụng voucher không
    public boolean isValid(BigDecimal orderAmount) {
        return LocalDateTime.now().isBefore(expiryDate) 
            && usedCount < usageLimit 
            && orderAmount.compareTo(minOrderAmount) >= 0;
    }
}