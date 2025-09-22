package com.example.purchaseorder.service;

import com.example.purchaseorder.dto.AuthResponse;
import com.example.purchaseorder.dto.LoginRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);
}
