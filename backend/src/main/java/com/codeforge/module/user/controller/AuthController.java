package com.codeforge.module.user.controller;

import com.codeforge.common.dto.ApiResponse;
import com.codeforge.module.user.dto.request.LoginRequest;
import com.codeforge.module.user.dto.request.RegisterRequest;
import com.codeforge.module.user.dto.response.AuthResponse;
import com.codeforge.module.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @com.codeforge.common.ratelimit.RateLimit(limit = 5, windowSeconds = 60, keyPrefix = "auth_register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(ApiResponse.success(response, "User registered successfully"), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @com.codeforge.common.ratelimit.RateLimit(limit = 10, windowSeconds = 60, keyPrefix = "auth_login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "User logged in successfully"));
    }
}
