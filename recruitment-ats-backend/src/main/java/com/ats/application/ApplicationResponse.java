package com.ats.application;

import java.time.LocalDateTime;

public record ApplicationResponse(
        Long id,
        CandidateSummary candidate,
        JobSummary job,
        LocalDateTime appliedAt,
        ApplicationStatus status) {

    public record CandidateSummary(Long id, Long userId, String name, String email) { }

    public record JobSummary(Long id, String title, String companyName) { }

    public static ApplicationResponse from(JobApplication application) {
        var candidateUser = application.getCandidate().getUser();
        var job = application.getJob();
        return new ApplicationResponse(
                application.getId(),
                new CandidateSummary(application.getCandidate().getId(), candidateUser.getId(),
                        candidateUser.getName(), candidateUser.getEmail()),
                new JobSummary(job.getId(), job.getTitle(), job.getCompany().getName()),
                application.getAppliedAt(),
                application.getStatus());
    }
}
