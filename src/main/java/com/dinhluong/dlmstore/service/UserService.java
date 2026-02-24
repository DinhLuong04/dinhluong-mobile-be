package com.dinhluong.dlmstore.service;

import com.dinhluong.dlmstore.dto.responses.UserDetailResponse;
import com.dinhluong.dlmstore.dto.responses.UserResponse;
import com.dinhluong.dlmstore.entity.Address;
import com.dinhluong.dlmstore.entity.Order;
import com.dinhluong.dlmstore.entity.Users;
import com.dinhluong.dlmstore.repository.AddressRepository;
import com.dinhluong.dlmstore.repository.OrderRepository;
import com.dinhluong.dlmstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;

    // SỬA LỖI: Đổi lại trả về List<UserResponse> vì UserDetailResponse không có roleName
    public List<UserResponse> getAdminUsers(String keyword, Boolean isEnabled) {
        List<Users> users = userRepository.searchAdminUsers(keyword, isEnabled);
        
        return users.stream().map(user -> UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .authProvider(user.getAuthProvider())
                .isEnabled(user.getIsEnabled())
                .roleName(user.getRole() != null ? user.getRole().getName() : null)
                .createdAt(user.getCreatedAt())
                .build()
        ).collect(Collectors.toList());
    }

    @Transactional
    public void toggleUserStatus(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
        
        if (user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getName())) {
            throw new RuntimeException("Không thể khóa tài khoản Quản trị viên!");
        }

        user.setIsEnabled(!user.getIsEnabled());
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserDetailResponse getUserDetail(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        List<Address> addresses = addressRepository.findByUserId(userId);
        List<UserDetailResponse.AddressDto> addressDtos = addresses.stream()
                .map(addr -> {
                    // Nối các thành phần lại thành một địa chỉ hoàn chỉnh
                    String fullAddress = addr.getStreet() + ", " + addr.getCity() + ", " + addr.getProvince() + ", " + addr.getCountry();
                    
                    return UserDetailResponse.AddressDto.builder()
                            .id(addr.getId())
                            // Lấy tên và sđt của User gán tạm vào vì Address không có
                            .receiverName(user.getFullName()) 
                            .receiverPhone(user.getPhone())
                            .fullAddress(fullAddress)
                            .build();
                })
                .collect(Collectors.toList());

        List<Order> recentOrders = orderRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId);
        List<UserDetailResponse.OrderDto> recentOrderDtos = recentOrders.stream()
                .map(order -> UserDetailResponse.OrderDto.builder()
                        .id(order.getId())
                        .totalAmount(order.getTotalAmount())
                        .status(order.getStatus().name())
                        .createdAt(order.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        long totalOrders = orderRepository.countByUserId(userId);
        int cancelledOrders = orderRepository.countByUserIdAndStatus(userId, Order.OrderStatus.CANCELLED);
        
        // SỬA LỖI: Dùng hàm mới an toàn hơn khi làm việc với Enum
        BigDecimal totalSpent = orderRepository.getTotalSpentByUserIdAndStatus(userId, Order.OrderStatus.DELIVERED);

        if (totalSpent == null) {
            totalSpent = BigDecimal.ZERO;
        }

        UserDetailResponse.UserStats stats = UserDetailResponse.UserStats.builder()
                .totalOrders(totalOrders)
                .cancelledOrders(cancelledOrders)
                .totalSpent(totalSpent)
                .build();

        return UserDetailResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .authProvider(user.getAuthProvider())
                .isEnabled(user.getIsEnabled())
                .createdAt(user.getCreatedAt())
                .statistics(stats)
                .addresses(addressDtos)
                .recentOrders(recentOrderDtos)
                .build();
    }
}