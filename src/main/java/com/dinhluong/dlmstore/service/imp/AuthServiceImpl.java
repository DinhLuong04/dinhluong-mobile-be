package com.dinhluong.dlmstore.service.imp;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dinhluong.dlmstore.dto.requests.RegisterRequest;
import com.dinhluong.dlmstore.entity.Roles;
import com.dinhluong.dlmstore.entity.Users;
import com.dinhluong.dlmstore.entity.Enums.AuthProvider;
import com.dinhluong.dlmstore.repository.RoleRepository;
import com.dinhluong.dlmstore.repository.UserRepository;
import com.dinhluong.dlmstore.service.AuthService;
import com.dinhluong.dlmstore.service.EmailService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

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
        user.setAuthProvider("LOCAL"); 

        user.setRole(userRole);
        user.setIsEnabled(false);
        String code = UUID.randomUUID().toString();
        user.setVerificationCode(code);
        user.setTokenExpiryDate(LocalDateTime.now().plusHours(24));

        userRepository.save(user);
        String apiLink = "http://localhost:8080/api/auth/verify?code=" + code;

        emailService.sendVerificationEmail(request.getEmail(), request.getFullName(), apiLink);
    }

   
    @Override
    public void verifyEmail(String verificationCode) {
        Users user = userRepository.findByVerificationCode(verificationCode)
                .orElseThrow(() -> new RuntimeException("Mã xác thực không hợp lệ"));

        if (Boolean.TRUE.equals(user.getIsEnabled())) {
            throw new RuntimeException("Tài khoản đã được kích hoạt trước đó");
        }

        if (user.getTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã xác thực đã hết hạn");
        }

        user.setIsEnabled(true);
        user.setVerificationCode(null);
        userRepository.save(user);
    }

    
    @Override
    public void resendVerificationCode(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        if (Boolean.TRUE.equals(user.getIsEnabled())) {
            throw new RuntimeException("Tài khoản đã kích hoạt rồi. Vui lòng đăng nhập.");
        }

        String newCode = UUID.randomUUID().toString();
        user.setVerificationCode(newCode);
        user.setTokenExpiryDate(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        String link = "http://localhost:8080/api/auth/verify?code=" + newCode;
        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), link);
    }

    
    @Override
    public void forgotPassword(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        int otpRandom = new Random().nextInt(900000) + 100000;
        String otp = String.valueOf(otpRandom);

        user.setResetPasswordToken(otp);
        user.setTokenExpiryDate(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        emailService.sendOtpEmail(email, otp);
    }

    
    @Override
    public void resetPassword(String email, String otp, String newPassword) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        if (user.getResetPasswordToken() == null || !user.getResetPasswordToken().equals(otp)) {
            throw new RuntimeException("Mã OTP không chính xác");
        }

        if (user.getTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã OTP đã hết hạn");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setTokenExpiryDate(null);
        userRepository.save(user);
    }

    @Override
    public Users Login(String email, String password) {

        Users user = userRepository.findByEmailWithRole(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        if (!Boolean.TRUE.equals(user.getIsEnabled())) {
            throw new RuntimeException("Tài khoản đã bị khóa");
        }

        
        if ("GOOGLE".equalsIgnoreCase(user.getAuthProvider())) {
            throw new RuntimeException("Vui lòng đăng nhập bằng Google");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Sai mật khẩu");
        }

        return user;
    }

}
