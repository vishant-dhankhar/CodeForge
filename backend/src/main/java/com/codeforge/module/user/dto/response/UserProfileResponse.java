package com.codeforge.module.user.dto.response;

import com.codeforge.common.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String username;
    private String email;
    private Role role;
    private Instant joinedAt;
    private UserStatsResponse stats;
}
