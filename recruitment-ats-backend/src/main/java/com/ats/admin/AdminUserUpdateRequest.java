package com.ats.admin;

import com.ats.user.Role;
import com.ats.user.UserStatus;

public record AdminUserUpdateRequest(
        String name,
        Role role,
        UserStatus status
) {}
