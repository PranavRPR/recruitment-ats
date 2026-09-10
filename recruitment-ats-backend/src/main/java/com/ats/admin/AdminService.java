package com.ats.admin;

import com.ats.application.JobApplication;
import com.ats.application.JobApplicationRepository;
import com.ats.job.Job;
import com.ats.job.JobRepository;
import com.ats.notification.Notification;
import com.ats.notification.NotificationRepository;
import com.ats.user.User;
import com.ats.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository applicationRepository;
    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public List<AdminUserResponse> users() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminUserResponse user(Long id) {
        return toResponse(findUser(id));
    }

    @Transactional
    public AdminUserResponse update(Long id, AdminUserUpdateRequest request) {
        User user = findUser(id);

        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name().trim());
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }
        if (request.status() != null) {
            user.setStatus(request.status());
        }

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found");
        }
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<AdminJobResponse> jobs() {
        return jobRepository.findAll().stream()
                .map(this::toJobResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminApplicationResponse> applications() {
        return applicationRepository.findAll().stream()
                .map(this::toApplicationResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminNotificationResponse> notifications() {
        return notificationRepository.findAll().stream()
                .map(this::toNotificationResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminReportResponse reports() {
        return new AdminReportResponse(
                userRepository.count(),
                jobRepository.count(),
                applicationRepository.count(),
                notificationRepository.count()
        );
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private AdminUserResponse toResponse(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus()
        );
    }

    private AdminJobResponse toJobResponse(Job job) {
        return new AdminJobResponse(
                job.getId(),
                job.getTitle(),
                job.getStatus(),
                job.getRecruiter().getId(),
                job.getCompany().getId()
        );
    }

    private AdminApplicationResponse toApplicationResponse(JobApplication application) {
        return new AdminApplicationResponse(
                application.getId(),
                application.getJob().getId(),
                application.getCandidate().getUser().getId(),
                application.getStatus(),
                application.getAppliedAt()
        );
    }

    private AdminNotificationResponse toNotificationResponse(Notification notification) {
        return new AdminNotificationResponse(
                notification.getId(),
                notification.getUser().getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
