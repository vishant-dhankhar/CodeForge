package com.codeforge.module.user.service;

import com.codeforge.module.user.dto.response.UserProfileResponse;

public interface UserService {

    UserProfileResponse getUserProfile(String username);
}
