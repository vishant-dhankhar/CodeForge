package com.codeforge.module.user.service.impl;

import com.codeforge.common.exception.ResourceNotFoundException;
import com.codeforge.module.user.dto.response.UserProfileResponse;
import com.codeforge.module.user.dto.response.UserStatsResponse;
import com.codeforge.module.user.entity.User;
import com.codeforge.module.user.entity.UserStats;
import com.codeforge.module.user.repository.UserRepository;
import com.codeforge.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserProfileResponse getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        UserStats stats = user.getStats();
        UserStatsResponse statsResponse;

        if (stats != null) {
            statsResponse = UserStatsResponse.builder()
                    .easySolved(stats.getEasySolved())
                    .mediumSolved(stats.getMediumSolved())
                    .hardSolved(stats.getHardSolved())
                    .totalSolved(stats.getTotalSolved())
                    .totalSubmissions(stats.getTotalSubmissions())
                    .acceptedSubmissions(stats.getAcceptedSubmissions())
                    .acceptanceRate(stats.getAcceptanceRate())
                    .build();
        } else {
            statsResponse = UserStatsResponse.builder().build();
        }

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .joinedAt(user.getCreatedAt())
                .stats(statsResponse)
                .build();
    }
}
