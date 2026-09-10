package com.ats.interview;

import com.ats.application.JobApplication;
import com.ats.application.JobApplicationRepository;
import com.ats.common.ResourceNotFoundException;
import com.ats.user.Role;
import com.ats.user.User;
import com.ats.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final JobApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    @Transactional
    public InterviewResponse create(InterviewRequest r, Authentication authentication) {
        JobApplication application = applicationRepository.findById(r.applicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        verifyStaffAccess(application, authentication);

        return InterviewResponse.from(interviewRepository.save(Interview.builder()
                .application(application)
                .type(r.type())
                .scheduledAt(r.scheduledAt())
                .durationMinutes(r.durationMinutes())
                .interviewers(r.interviewers())
                .meetingLink(r.meetingLink())
                .notes(r.notes())
                .status(InterviewStatus.SCHEDULED)
                .build()));
    }

    @Transactional
    public InterviewResponse updateStatus(Long id, InterviewStatus status, Authentication authentication) {
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found"));
        verifyStaffAccess(interview.getApplication(), authentication);
        interview.setStatus(status);
        return InterviewResponse.from(interviewRepository.save(interview));
    }

    @Transactional(readOnly = true)
    public List<InterviewResponse> my(Authentication authentication) {
        User user = currentUser(authentication);
        List<Interview> interviews;
        if (user.getRole() == Role.CANDIDATE) {
            interviews = interviewRepository.findByApplicationCandidateUserId(user.getId());
        } else if (user.getRole() == Role.ADMIN) {
            interviews = interviewRepository.findAll();
        } else if (user.getRole() == Role.RECRUITER) {
            interviews = interviewRepository.findByApplicationJobRecruiterId(user.getId());
        } else {
            interviews = interviewRepository.findByApplicationJobCompanyOwnerId(user.getId());
        }
        return interviews.stream().map(InterviewResponse::from).toList();
    }

    private void verifyStaffAccess(com.ats.application.JobApplication application,
                                   Authentication authentication) {
        User user = currentUser(authentication);
        if (user.getRole() == Role.ADMIN) return;
        boolean recruiterOwnsJob = application.getJob().getRecruiter().getId().equals(user.getId());
        boolean managerOwnsCompany = application.getJob().getCompany().getOwner().getId().equals(user.getId());
        boolean authorizedManager = user.getRole() == Role.HIRING_MANAGER && managerOwnsCompany;
        if ((user.getRole() != Role.RECRUITER && user.getRole() != Role.HIRING_MANAGER)
            || (!recruiterOwnsJob && !authorizedManager)) {
            throw new AccessDeniedException("You are not authorized to manage this interview");
        }
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
    }
}
