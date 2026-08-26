package com.codeforge.module.user.service;

import com.codeforge.module.user.dto.request.LoginRequest;
import com.codeforge.module.user.dto.request.RegisterRequest;
import com.codeforge.module.user.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
