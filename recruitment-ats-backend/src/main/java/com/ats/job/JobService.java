package com.ats.job;

import com.ats.company.Company;
import com.ats.company.CompanyRepository;
import com.ats.common.ResourceNotFoundException;
import com.ats.user.User;
import com.ats.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {
    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Transactional
    public Job create(JobRequest r, Authentication authentication) {
        User currentUser = currentUser(authentication);
        if (currentUser.getRole() != com.ats.user.Role.RECRUITER) {
            throw new AccessDeniedException("Only recruiters can create jobs");
        }

        Company company = companyRepository.findById(r.companyId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Company not found: " + r.companyId()));

        if (!company.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not own this company");
        }

        return jobRepository.save(Job.builder()
                .title(r.title()).description(r.description()).department(r.department())
                .location(r.location()).employmentType(r.employmentType())
                .experienceRequired(r.experienceRequired()).minSalary(r.minSalary())
                .maxSalary(r.maxSalary()).requiredSkills(r.requiredSkills())
                .preferredSkills(r.preferredSkills()).education(r.education())
                .responsibilities(r.responsibilities()).qualifications(r.qualifications())
                .applicationDeadline(r.applicationDeadline()).openings(r.openings())
                .company(company).recruiter(currentUser).status(JobStatus.DRAFT).build());
    }

    @Transactional
    public Job updateStatus(Long id, JobStatus status, Authentication authentication) {
        Job job = get(id);
        verifyJobAccess(job, authentication);
        job.setStatus(status);
        return jobRepository.save(job);
    }

    @Transactional(readOnly = true)
    public Job get(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));
    }

    @Transactional(readOnly = true)
    public List<Job> published() {
        return jobRepository.findByStatus(JobStatus.PUBLISHED);
    }

    @Transactional(readOnly = true)
    public List<Job> byRecruiter(Long recruiterId, Authentication authentication) {
        User currentUser = currentUser(authentication);
        if (currentUser.getRole() != com.ats.user.Role.ADMIN
                && !currentUser.getId().equals(recruiterId)) {
            throw new AccessDeniedException("You can only view your own jobs");
        }
        return jobRepository.findByRecruiterId(recruiterId);
    }

    private void verifyJobAccess(Job job, Authentication authentication) {
        User currentUser = currentUser(authentication);
        if (currentUser.getRole() == com.ats.user.Role.ADMIN) {
            return;
        }
        if (currentUser.getRole() != com.ats.user.Role.RECRUITER
                || !job.getRecruiter().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not own this job");
        }
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
    }
}
