package com.ats.admin;

import com.ats.application.ApplicationStatus;

import java.time.LocalDateTime;

public record AdminApplicationResponse(
        Long id,
        Long jobId,
        Long candidateUserId,
        ApplicationStatus status,
        LocalDateTime appliedAt
) {}
