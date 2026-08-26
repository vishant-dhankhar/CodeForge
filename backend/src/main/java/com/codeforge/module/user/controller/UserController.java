package com.codeforge.module.user.controller;

import com.codeforge.common.dto.ApiResponse;
import com.codeforge.module.user.dto.response.UserProfileResponse;
import com.codeforge.module.user.service.UserService;
import com.codeforge.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        UserProfileResponse response = userService.getUserProfile(currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Current user profile fetched successfully"));
    }

    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(
            @PathVariable String username
    ) {
        UserProfileResponse response = userService.getUserProfile(username);
        return ResponseEntity.ok(ApiResponse.success(response, "User profile fetched successfully"));
    }
}
