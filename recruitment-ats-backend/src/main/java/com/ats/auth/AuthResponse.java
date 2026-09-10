package com.ats.auth;

public record AuthResponse(
        String token,
        Long userId,
        String name,
        String email,
        String role
) {}
