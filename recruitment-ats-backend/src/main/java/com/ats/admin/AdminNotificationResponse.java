package com.ats.admin;

import java.time.LocalDateTime;

public record AdminNotificationResponse(
        Long id,
        Long userId,
        String title,
        String message,
        boolean read,
        LocalDateTime createdAt
) {}
