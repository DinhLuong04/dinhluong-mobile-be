package com.dinhluong.dlmstore.dto.responses;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private String customerName;
    private String method;
    private BigDecimal amount;
    private String status;
    private String transactionId;
    private LocalDateTime paidAt;
}