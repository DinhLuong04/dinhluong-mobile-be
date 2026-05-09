package com.dinhluong.dlmstore.dto.responses;

import com.dinhluong.dlmstore.entity.Order;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private BigDecimal totalAmount;
    private Order.OrderStatus status;
    private LocalDateTime createdAt;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String paymentMethod;
    private String paymentStatus;
    private String reason;
    private String userNote;
    private LocalDateTime deliveredAt;
    private BigDecimal discountAmount;
    private String cancelledBy;
    private List<OrderItemResponse> items;
}
