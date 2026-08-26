package com.codeforge.module.user.controller;

import com.codeforge.common.enums.Role;
import com.codeforge.common.exception.ConflictException;
import com.codeforge.common.exception.GlobalExceptionHandler;
import com.codeforge.module.user.dto.request.LoginRequest;
import com.codeforge.module.user.dto.request.RegisterRequest;
import com.codeforge.module.user.dto.response.AuthResponse;
import com.codeforge.module.user.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/auth/register should return 201 Created with JWT")
    void shouldRegisterUser() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("john_doe")
                .email("john@example.com")
                .password("password123")
                .build();

        AuthResponse response = AuthResponse.builder()
                .token("jwt-token-xyz")
                .userId(1L)
                .username("john_doe")
                .email("john@example.com")
                .role(Role.USER)
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-token-xyz"))
                .andExpect(jsonPath("$.data.username").value("john_doe"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register should return 409 Conflict when username exists")
    void shouldReturnConflictOnDuplicateUsername() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("john_doe")
                .email("john@example.com")
                .password("password123")
                .build();

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new ConflictException("Username is already taken: john_doe"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_CONFLICT"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login should return 200 OK with JWT")
    void shouldLoginUser() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("john_doe")
                .password("password123")
                .build();

        AuthResponse response = AuthResponse.builder()
                .token("jwt-login-token")
                .userId(1L)
                .username("john_doe")
                .email("john@example.com")
                .role(Role.USER)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-login-token"));
    }
}
