package com.codeforge.module.user.controller;

import com.codeforge.common.enums.Role;
import com.codeforge.common.exception.GlobalExceptionHandler;
import com.codeforge.common.exception.ResourceNotFoundException;
import com.codeforge.module.user.dto.response.UserProfileResponse;
import com.codeforge.module.user.dto.response.UserStatsResponse;
import com.codeforge.module.user.service.UserService;
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

import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/users/{username} should return 200 OK with difficulty statistics")
    void shouldReturnUserProfileWithStats() throws Exception {
        UserStatsResponse stats = UserStatsResponse.builder()
                .easySolved(5)
                .mediumSolved(3)
                .hardSolved(1)
                .totalSolved(9)
                .totalSubmissions(15)
                .acceptedSubmissions(9)
                .acceptanceRate(60.0)
                .build();

        UserProfileResponse profile = UserProfileResponse.builder()
                .id(1L)
                .username("john_doe")
                .email("john@example.com")
                .role(Role.USER)
                .joinedAt(Instant.now())
                .stats(stats)
                .build();

        when(userService.getUserProfile("john_doe")).thenReturn(profile);

        mockMvc.perform(get("/api/v1/users/john_doe")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("john_doe"))
                .andExpect(jsonPath("$.data.stats.easySolved").value(5))
                .andExpect(jsonPath("$.data.stats.mediumSolved").value(3))
                .andExpect(jsonPath("$.data.stats.hardSolved").value(1))
                .andExpect(jsonPath("$.data.stats.totalSolved").value(9))
                .andExpect(jsonPath("$.data.stats.acceptanceRate").value(60.0));
    }

    @Test
    @DisplayName("GET /api/v1/users/{username} should return 404 when user does not exist")
    void shouldReturn404WhenUserNotFound() throws Exception {
        when(userService.getUserProfile("unknown_user"))
                .thenThrow(new ResourceNotFoundException("User", "username", "unknown_user"));

        mockMvc.perform(get("/api/v1/users/unknown_user")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }
}
