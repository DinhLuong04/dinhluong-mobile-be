package com.dinhluong.dlmstore.dto.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderStatsResponse {
    private long pending;              // Chờ xác nhận
    private long processing;           // Đang xử lý
    private long shipped;              // Đang giao
    private long delivered;            // Thành công
    private long cancelledOrReturned;  // Hủy hoặc Hoàn
    private long total;                // Tổng số đơn hàng
}