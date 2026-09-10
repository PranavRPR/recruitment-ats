package com.ats.application;

import com.ats.candidate.CandidateProfile;
import com.ats.candidate.CandidateProfileRepository;
import com.ats.job.Job;
import com.ats.job.JobRepository;
import com.ats.user.Role;
import com.ats.user.User;
import com.ats.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final CandidateProfileRepository candidateRepository;
    private final UserRepository userRepository;

    @Transactional
    public JobApplication apply(Long jobId, Long candidateUserId, Authentication authentication) {
        User currentUser = currentUser(authentication);
        if (currentUser.getRole() != Role.CANDIDATE
                || !currentUser.getId().equals(candidateUserId)) {
            throw new AccessDeniedException("Candidates can only apply for themselves");
        }

        if (applicationRepository.existsByJobIdAndCandidateUserId(jobId, candidateUserId)) {
            throw new IllegalArgumentException("You have already applied for this job");
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));
        if (job.getStatus() != com.ats.job.JobStatus.PUBLISHED
            || (job.getApplicationDeadline() != null
            && job.getApplicationDeadline().isBefore(LocalDate.now()))) {
            throw new IllegalArgumentException("Applications are closed for this job");
        }

        CandidateProfile candidate = candidateRepository.findByUserId(candidateUserId)
                .orElseThrow(() -> new IllegalArgumentException("Complete your candidate profile first"));

        return applicationRepository.save(JobApplication.builder()
                .job(job)
                .candidate(candidate)
                .status(ApplicationStatus.APPLIED)
                .appliedAt(LocalDateTime.now())
                .build());
    }

    @Transactional(readOnly = true)
    public List<JobApplication> byCandidate(Long userId, Authentication authentication) {
        User currentUser = currentUser(authentication);
        if (currentUser.getRole() != Role.ADMIN
                && !currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("You can only view your own applications");
        }
        return applicationRepository.findByCandidateUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<JobApplication> byJob(Long jobId, Authentication authentication) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));
        verifyStaffAccess(job, authentication);
        return applicationRepository.findByJobId(jobId);
    }

    @Transactional
    public JobApplication updateStatus(Long id, ApplicationStatus status, Authentication authentication) {
        JobApplication app = applicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        verifyStaffAccess(app.getJob(), authentication);
        if (!isValidTransition(app.getStatus(), status)) {
            throw new IllegalArgumentException(
                    "Invalid application status transition from " + app.getStatus() + " to " + status);
        }
        app.setStatus(status);
        return applicationRepository.save(app);
    }

    private boolean isValidTransition(ApplicationStatus current, ApplicationStatus next) {
        if (current == next) {
            return true;
        }
        if (next == ApplicationStatus.REJECTED) {
            return current != ApplicationStatus.HIRED;
        }
        return switch (current) {
            case APPLIED -> next == ApplicationStatus.SCREENING;
            case SCREENING -> next == ApplicationStatus.SHORTLISTED;
            case SHORTLISTED -> next == ApplicationStatus.INTERVIEW;
            case INTERVIEW -> next == ApplicationStatus.TECHNICAL_ROUND
                    || next == ApplicationStatus.OFFER;
            case TECHNICAL_ROUND -> next == ApplicationStatus.HR_ROUND
                    || next == ApplicationStatus.OFFER;
            case HR_ROUND, OFFER -> next == ApplicationStatus.HIRED;
            case HIRED, REJECTED -> false;
        };
    }

    private void verifyStaffAccess(Job job, Authentication authentication) {
        User currentUser = currentUser(authentication);
        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }
        if ((currentUser.getRole() != Role.RECRUITER
                && currentUser.getRole() != Role.HIRING_MANAGER)
                || !job.getRecruiter().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to manage this job");
        }
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
    }
}
