package com.ats.interview;

import com.ats.application.JobApplication;

import java.time.LocalDateTime;

public record InterviewResponse(
        Long interviewId,
        Long applicationId,
        Long jobId,
        String jobTitle,
        Long candidateId,
        String candidateName,
        InterviewType type,
        LocalDateTime scheduledAt,
        Integer durationMinutes,
        String interviewers,
        String meetingLink,
        String notes,
        InterviewStatus status) {

    public static InterviewResponse from(Interview interview) {
        JobApplication application = interview.getApplication();
        var candidateUser = application.getCandidate().getUser();
        var job = application.getJob();
        return new InterviewResponse(
                interview.getId(),
                application.getId(),
                job.getId(),
                job.getTitle(),
                candidateUser.getId(),
                candidateUser.getName(),
                interview.getType(),
                interview.getScheduledAt(),
                interview.getDurationMinutes(),
                interview.getInterviewers(),
                interview.getMeetingLink(),
                interview.getNotes(),
                interview.getStatus());
    }
}
