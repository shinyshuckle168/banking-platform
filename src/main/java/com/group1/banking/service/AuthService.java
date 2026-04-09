package com.group1.banking.service;

import com.group1.banking.dto.auth.AuthResponse;
import com.group1.banking.dto.auth.LoginRequest;
import com.group1.banking.dto.auth.RegisterRequest;
import com.group1.banking.dto.auth.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
