package com.dinhluong.dlmstore.dto.responses;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UserDetailResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String avatarUrl;
    private String authProvider;
    private Boolean isEnabled;
    private LocalDateTime createdAt;
    
    private UserStats statistics;
    private List<AddressDto> addresses;    // THÊM MỚI
    private List<OrderDto> recentOrders;   // THÊM MỚI

    @Data
    @Builder
    public static class UserStats {
        private long totalOrders;
        private int cancelledOrders;
        private BigDecimal totalSpent;
    }

    // Class phụ để hứng Địa chỉ
    @Data
    @Builder
    public static class AddressDto {
        private Long id;
        // Ghi chú: Bạn tự đổi tên trường này cho khớp với Entity Address thực tế của bạn nhé
        private String receiverName; 
        private String receiverPhone;
        private String fullAddress; 
    }

    // Class phụ để hứng Đơn hàng gọn nhẹ
    @Data
    @Builder
    public static class OrderDto {
        private Long id;
        private BigDecimal totalAmount;
        private String status;
        private LocalDateTime createdAt;
    }
}