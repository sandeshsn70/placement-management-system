package com.placement.service;

import com.placement.dto.auth.AuthResponse;
import com.placement.dto.auth.LoginRequest;
import com.placement.dto.auth.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
