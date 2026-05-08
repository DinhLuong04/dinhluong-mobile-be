package com.dinhluong.dlmstore.service;

import com.dinhluong.dlmstore.dto.requests.RegisterRequest;
import com.dinhluong.dlmstore.entity.Users;

public interface AuthService {
    void verifyEmail(String verificationCode);

    void resendVerificationCode(String email);

    void forgotPassword(String email);

    void resetPassword(String email, String otp, String newPassword);

    Users Login(String email, String password);
    Users adminLogin(String email, String password);

    void register(RegisterRequest request);

}