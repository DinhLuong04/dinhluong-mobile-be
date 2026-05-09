package com.dinhluong.dlmstore.dto.requests;

import com.dinhluong.dlmstore.entity.Order;
import lombok.Data;
import java.util.List;

@Data
public class BulkStatusRequest {
    private List<Long> orderIds;
    private Order.OrderStatus newStatus;
    private String reason;
}