package com.dinhluong.dlmstore.entity;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "address_id")
    private Long addressId;

    @Column(name = "voucher_id")
    private Long voucherId;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // Enum: PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED

    @Column(name = "receiver_name", length = 100)
    private String receiverName;

    @Column(name = "receiver_phone", length = 20)
    private String receiverPhone;

    @Column(name = "receiver_address", columnDefinition = "TEXT")
    private String receiverAddress;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = OrderStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum OrderStatus {
        PENDING, PROCESSING, SHIPPED, DELIVERED, RETURNED, CANCELLED // Thêm RETURNED
    }
}