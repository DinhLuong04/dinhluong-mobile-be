package com.dinhluong.dlmstore.service;



import com.cloudinary.Cloudinary;
import com.dinhluong.dlmstore.dto.requests.ChangePasswordRequest;
import com.dinhluong.dlmstore.dto.responses.UserProfileResponse;
import com.dinhluong.dlmstore.dto.responses.UserProfileStatsResponse;
import com.dinhluong.dlmstore.entity.Order;
import com.dinhluong.dlmstore.entity.Users;
import com.dinhluong.dlmstore.repository.OrderRepository;
import com.dinhluong.dlmstore.repository.UserRepository; // Giả định bạn có repo này

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import com.cloudinary.utils.ObjectUtils;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final Cloudinary cloudinary;
    private final PasswordEncoder passwordEncoder;
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

    public UserProfileResponse getUserProfile(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, String fullName, String phone, MultipartFile avatar) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        if (fullName != null) user.setFullName(fullName);
        if (phone != null) user.setPhone(phone);

        // 🔥 Logic xử lý upload lên Cloudinary
        if (avatar != null && !avatar.isEmpty()) {
            try {
                // Upload file lên Cloudinary
                Map<String, Object> uploadResult = cloudinary.uploader().upload(avatar.getBytes(), ObjectUtils.emptyMap());
                
                // Lấy URL an toàn (https) do Cloudinary trả về
                String imageUrl = uploadResult.get("secure_url").toString();
                
                // Lưu URL này vào Database
                user.setAvatarUrl(imageUrl); 
                
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi tải ảnh lên Cloudinary", e);
            }
        }

        userRepository.save(user);

        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        // 1. Kiểm tra mật khẩu mới và xác nhận có khớp không
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu mới không khớp với xác nhận mật khẩu");
        }

        // 2. Tìm user
        Users user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // 3. Kiểm tra mật khẩu cũ có đúng không
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu hiện tại không chính xác");
        }

        // 4. Mã hóa mật khẩu mới và lưu
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        
        // (Tùy chọn) Nếu Entity Users của bạn có trường lưu thời gian đổi pass:
        // user.setLastPasswordChange(LocalDateTime.now());
        
        userRepository.save(user);
    }
}