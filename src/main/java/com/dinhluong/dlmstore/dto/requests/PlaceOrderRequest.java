package com.dinhluong.dlmstore.dto.requests;

import lombok.Data;
import java.util.List;

@Data
public class PlaceOrderRequest {
    private Long userId;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String note;
    private String paymentMethod;
    private String voucherCode;
    
    // Đổi tên field từ cartItems thành items cho tổng quát hơn
    private List<PlaceOrderItemRequest> items; 
}

