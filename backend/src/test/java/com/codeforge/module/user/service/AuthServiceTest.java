package com.codeforge.module.user.service;

import com.codeforge.common.enums.Role;
import com.codeforge.common.exception.ConflictException;
import com.codeforge.common.exception.UnauthorizedException;
import com.codeforge.module.user.dto.request.LoginRequest;
import com.codeforge.module.user.dto.request.RegisterRequest;
import com.codeforge.module.user.dto.response.AuthResponse;
import com.codeforge.module.user.entity.User;
import com.codeforge.module.user.entity.UserStats;
import com.codeforge.module.user.repository.UserRepository;
import com.codeforge.module.user.service.impl.AuthServiceImpl;
import com.codeforge.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("Should successfully register new user and return AuthResponse")
    void shouldRegisterNewUser() {
        RegisterRequest request = RegisterRequest.builder()
                .username("john_doe")
                .email("john@example.com")
                .password("securePassword123")
                .build();

        when(userRepository.existsByUsername("john_doe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("securePassword123")).thenReturn("hashedPassword");

        User savedUser = User.builder()
                .id(1L)
                .username("john_doe")
                .email("john@example.com")
                .passwordHash("hashedPassword")
                .role(Role.USER)
                .stats(UserStats.builder().build())
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(tokenProvider.generateTokenFromUsername("john_doe")).thenReturn("mock-jwt-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("john_doe", response.getUsername());
        assertEquals("john@example.com", response.getEmail());
        assertEquals(Role.USER, response.getRole());
    }

    @Test
    @DisplayName("Should reject registration when username is already taken")
    void shouldRejectDuplicateUsername() {
        RegisterRequest request = RegisterRequest.builder()
                .username("john_doe")
                .email("john@example.com")
                .password("securePassword123")
                .build();

        when(userRepository.existsByUsername("john_doe")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));
    }

    @Test
    @DisplayName("Should successfully login and return AuthResponse")
    void shouldLoginSuccessfully() {
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("john_doe")
                .password("securePassword123")
                .build();

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

        User user = User.builder()
                .id(1L)
                .username("john_doe")
                .email("john@example.com")
                .role(Role.USER)
                .build();

        when(userRepository.findByUsernameOrEmail("john_doe")).thenReturn(Optional.of(user));
        when(tokenProvider.generateToken(auth)).thenReturn("mock-login-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock-login-token", response.getToken());
        assertEquals("john_doe", response.getUsername());
    }

    @Test
    @DisplayName("Should throw UnauthorizedException on invalid credentials")
    void shouldThrowUnauthorizedOnBadCredentials() {
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("john_doe")
                .password("wrongPassword")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }
}
