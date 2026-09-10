package com.ats.admin;

import com.ats.job.JobStatus;

public record AdminJobResponse(
        Long id,
        String title,
        JobStatus status,
        Long recruiterId,
        Long companyId
) {}
