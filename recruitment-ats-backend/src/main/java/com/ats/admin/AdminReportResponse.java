package com.ats.admin;

public record AdminReportResponse(
        long userCount,
        long jobCount,
        long applicationCount,
        long notificationCount
) {}
