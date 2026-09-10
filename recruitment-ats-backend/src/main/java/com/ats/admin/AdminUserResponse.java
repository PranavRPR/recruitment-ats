package com.ats.admin;

import com.ats.user.Role;
import com.ats.user.UserStatus;

public record AdminUserResponse(
        Long id,
        String name,
        String email,
        Role role,
        UserStatus status
) {}
