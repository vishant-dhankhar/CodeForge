package com.codeforge.module.user.dto.response;

import com.codeforge.common.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    @Builder.Default
    private String type = "Bearer";
    private Long userId;
    private String username;
    private String email;
    private Role role;
}
