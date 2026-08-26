package com.codeforge.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long expirationMs = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(secret, expirationMs);
    }

    @Test
    @DisplayName("Should generate valid JWT and parse username correctly")
    void shouldGenerateAndValidateToken() {
        String username = "alex_coder";
        String token = jwtTokenProvider.generateTokenFromUsername(username);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(username, jwtTokenProvider.getUsernameFromJWT(token));
    }

    @Test
    @DisplayName("Should reject invalid or malformed JWT token")
    void shouldRejectInvalidToken() {
        String invalidToken = "invalid.token.payload";

        assertFalse(jwtTokenProvider.validateToken(invalidToken));
    }

    @Test
    @DisplayName("Should reject expired JWT token")
    void shouldRejectExpiredToken() {
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(secret, -1000); // Already expired
        String expiredToken = shortLivedProvider.generateTokenFromUsername("alex_coder");

        assertFalse(jwtTokenProvider.validateToken(expiredToken));
    }
}
