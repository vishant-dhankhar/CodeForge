package com.codeforge.module.user.service.impl;

import com.codeforge.common.enums.Role;
import com.codeforge.common.exception.ConflictException;
import com.codeforge.common.exception.UnauthorizedException;
import com.codeforge.module.user.dto.request.LoginRequest;
import com.codeforge.module.user.dto.request.RegisterRequest;
import com.codeforge.module.user.dto.response.AuthResponse;
import com.codeforge.module.user.entity.User;
import com.codeforge.module.user.entity.UserStats;
import com.codeforge.module.user.repository.UserRepository;
import com.codeforge.module.user.service.AuthService;
import com.codeforge.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username is already taken: " + request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already in use: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .isActive(true)
                .build();

        UserStats stats = UserStats.builder()
                .user(user)
                .easySolved(0)
                .mediumSolved(0)
                .hardSolved(0)
                .totalSubmissions(0)
                .acceptedSubmissions(0)
                .build();

        user.setStats(stats);
        User savedUser = userRepository.save(user);

        String token = tokenProvider.generateTokenFromUsername(savedUser.getUsername());

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
            );

            User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail())
                    .orElseThrow(() -> new UnauthorizedException("Invalid username/email or password"));

            String token = tokenProvider.generateToken(authentication);

            return AuthResponse.builder()
                    .token(token)
                    .type("Bearer")
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .build();
        } catch (BadCredentialsException ex) {
            throw new UnauthorizedException("Invalid username/email or password");
        }
    }
}
