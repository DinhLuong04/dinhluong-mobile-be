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
    private List<PlaceOrderItemRequest> items;
}
