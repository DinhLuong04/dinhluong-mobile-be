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
    
    // 🔥 3 trường này đang bị thiếu nên Builder báo lỗi
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String paymentMethod;
    private String paymentStatus;
    // Đổi 'I' hoa thành 'i' thường cho chuẩn quy tắc Java
    private List<OrderItemResponse> items;
}
