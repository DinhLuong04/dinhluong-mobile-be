package com.dinhluong.dlmstore.service;

import com.dinhluong.dlmstore.dto.requests.RegisterRequest;
import com.dinhluong.dlmstore.entity.Users;

public interface AuthService {
    Users Login(String email, String password);

    void register(RegisterRequest request);
    
}