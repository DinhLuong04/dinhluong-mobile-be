package com.dinhluong.dlmstore.service.imp;

import java.time.LocalDateTime;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dinhluong.dlmstore.dto.requests.RegisterRequest;
import com.dinhluong.dlmstore.entity.Roles;
import com.dinhluong.dlmstore.entity.Users;
import com.dinhluong.dlmstore.repository.RoleRepository;
import com.dinhluong.dlmstore.repository.UserRepository;
import com.dinhluong.dlmstore.service.AuthService;

import jakarta.transaction.Transactional;
@Service
@Transactional
public class AuthServiceImpl implements AuthService {
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private  RoleRepository roleRepository;
    @Autowired
    private  PasswordEncoder passwordEncoder;

    @Override
    public Users Login(String email, String password) {

        Users user = userRepository.findByEmailWithRole(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        if (!Boolean.TRUE.equals(user.getIsEnabled())) {
            throw new RuntimeException("Tài khoản đã bị khóa");
        }

        // Tài khoản Google không cho login bằng password
        if ("GOOGLE".equalsIgnoreCase(user.getAuthProvider())) {
            throw new RuntimeException("Vui lòng đăng nhập bằng Google");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Sai mật khẩu");
        }

        return user;
    }

    @Override
    public void register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        Roles userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Role USER không tồn tại"));

        Users user = new Users();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(null);
        user.setAvatarUrl(null); // user tự set sau
        user.setAuthProvider("LOCAL");
        user.setProviderId(null);
        user.setRole(userRole);
        user.setIsEnabled(false); // chờ verify OTP
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        // 👉 Nếu bạn có OTP / EmailService thì gọi ở đây
        // otpService.sendOtp(user.getEmail());
    }
    
}
