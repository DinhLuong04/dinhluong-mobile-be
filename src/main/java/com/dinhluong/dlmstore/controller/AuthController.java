package com.dinhluong.dlmstore.controller;

import com.dinhluong.dlmstore.dto.ApiResponse;
import com.dinhluong.dlmstore.dto.requests.LoginRequest;
import com.dinhluong.dlmstore.dto.requests.Oauth2LoginRequest;
import com.dinhluong.dlmstore.dto.requests.RegisterRequest;
import com.dinhluong.dlmstore.dto.responses.LoginResponse;
import com.dinhluong.dlmstore.entity.Users;
import com.dinhluong.dlmstore.repository.UserRepository;
import com.dinhluong.dlmstore.service.AuthService;
import com.dinhluong.dlmstore.service.FacebookService;
import com.dinhluong.dlmstore.service.GoogleService;
import com.dinhluong.dlmstore.utils.JwtTokenProvider;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.nio.file.attribute.UserPrincipal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtTokenProvider tokenProvider;
    @Autowired
    private AuthService authService;
    @Autowired
    private GoogleService googleService;
    @Autowired
    private FacebookService facebookService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

        Users user = authService.Login(request.getEmail(), request.getPassword());

        String token = tokenProvider.generateToken(user);

        String accountType = "GOOGLE".equalsIgnoreCase(user.getAuthProvider())
                ? "GOOGLE"
                : "NORMAL";

        return ApiResponse.success(
                "Đăng nhập thành công",
                new LoginResponse(
                        token,
                        user.getId(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getAvatarUrl(),
                        accountType));
    }

    @PostMapping("/register")
    public ApiResponse<?> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.success("Đăng ký thành công! Vui lòng kiểm tra email để kích hoạt.", null);
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam("code") String code) {
        try {
            authService.verifyEmail(code);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("https://localhost:5173/login?verified=true"))
                    .build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("https://localhost:5173/login?verified=false&error=" + e.getMessage()))
                    .build();
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestParam String email) {
        try {
            authService.resendVerificationCode(email);
            return ResponseEntity.ok("Đã gửi lại email xác thực.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        try {
            authService.forgotPassword(email);
            return ResponseEntity.ok(Collections.singletonMap("message", "Mã OTP đã được gửi đến email của bạn."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String otp = request.get("otp");
            String newPassword = request.get("newPassword");

            authService.resetPassword(email, otp, newPassword);
            return ResponseEntity
                    .ok(Collections.singletonMap("message", "Đổi mật khẩu thành công! Hãy đăng nhập lại."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @PostMapping("/google-login")
    public ResponseEntity<?> googleLogin(@RequestBody Oauth2LoginRequest request) {
        String idToken = request.getIdToken();

        if (idToken == null || idToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing id_token"));
        }

        try {
            Object response = googleService.googleLogin(idToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/facebook-login")
    public ResponseEntity<?> facebookLogin(@RequestBody Oauth2LoginRequest request) {
        String accessToken = request.getIdToken();
        if (accessToken == null || accessToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing access_token"));
        }
        try {

            Object response = facebookService.facebookLogin(accessToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

}
