package com.dinhluong.dlmstore.service;



import com.dinhluong.dlmstore.dto.responses.UserProfileStatsResponse;
import com.dinhluong.dlmstore.entity.Order;
import com.dinhluong.dlmstore.entity.Users;
import com.dinhluong.dlmstore.repository.OrderRepository;
import com.dinhluong.dlmstore.repository.UserRepository; // Giả định bạn có repo này
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public UserProfileStatsResponse getUserStats(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        // Lấy tổng tiền đã chi tiêu (Chỉ tính đơn DELIVERED)
        BigDecimal totalSpent = orderRepository.getTotalSpentByUserId(userId);
        if (totalSpent == null) totalSpent = BigDecimal.ZERO;

        // Đếm tổng số đơn hàng đã hoàn thành
        Integer totalOrders = orderRepository.countByUserIdAndStatus(userId, Order.OrderStatus.DELIVERED);
        if (totalOrders == null) totalOrders = 0;

        // Logic chia hạng giả lập (Bạn có thể tùy chỉnh theo business của bạn)
        String rank = "S-NULL";
        String nextRankName = "S-NEW";
        BigDecimal nextRankMoney = new BigDecimal("3000000").subtract(totalSpent);

        if (totalSpent.compareTo(new BigDecimal("3000000")) >= 0) {
            rank = "S-NEW";
            nextRankName = "S-VIP";
            nextRankMoney = new BigDecimal("10000000").subtract(totalSpent);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return UserProfileStatsResponse.builder()
                .name(user.getFullName() != null ? user.getFullName() : "Khách hàng")
                .phone(user.getPhone() != null ? user.getPhone() : "Chưa cập nhật")
                .rank(rank)
                .updateDate("01/01/2027") // Có thể tính toán dựa trên ngày hiện tại
                .orders(totalOrders)
                .money(totalSpent)
                .nextRankMoney(nextRankMoney.compareTo(BigDecimal.ZERO) > 0 ? nextRankMoney.toString() : "0")
                .nextRankName(nextRankName)
                .startDate(user.getCreatedAt() != null ? user.getCreatedAt().format(formatter) : "Gần đây")
                .build();
    }
}