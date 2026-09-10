package com.ats.interview;

import java.time.LocalDateTime;

public record InterviewRequest(
        Long applicationId,
        InterviewType type,
        LocalDateTime scheduledAt,
        Integer durationMinutes,
        String interviewers,
        String meetingLink,
        String notes
) {}
