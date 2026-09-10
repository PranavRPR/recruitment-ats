package com.ats.job;

import com.ats.company.Company;
import com.ats.company.CompanyRepository;
import com.ats.common.ResourceNotFoundException;
import com.ats.user.Role;
import com.ats.user.User;
import com.ats.user.UserRepository;
import com.ats.user.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private JobService jobService;

    @Test
    void recruiterCreatesJobUsingAuthenticatedRecruiterAndOwnedCompany() {
        User recruiter = user(7L, "recruiter@ats.com", Role.RECRUITER);
        Company company = company(15L, recruiter);
        when(authentication.getName()).thenReturn(recruiter.getEmail());
        when(userRepository.findByEmailIgnoreCase(recruiter.getEmail())).thenReturn(Optional.of(recruiter));
        when(companyRepository.findById(15L)).thenReturn(Optional.of(company));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Job result = jobService.create(request(15L, 999L), authentication);

        assertThat(result.getCompany()).isSameAs(company);
        assertThat(result.getRecruiter()).isSameAs(recruiter);
        verify(jobRepository).save(any(Job.class));
    }

    @Test
    void recruiterCannotUseAnotherRecruitersCompany() {
        User recruiter = user(7L, "recruiter@ats.com", Role.RECRUITER);
        User otherRecruiter = user(9L, "other@ats.com", Role.RECRUITER);
        when(authentication.getName()).thenReturn(recruiter.getEmail());
        when(userRepository.findByEmailIgnoreCase(recruiter.getEmail())).thenReturn(Optional.of(recruiter));
        when(companyRepository.findById(15L)).thenReturn(Optional.of(company(15L, otherRecruiter)));

        assertThatThrownBy(() -> jobService.create(request(15L, 7L), authentication))
                .isInstanceOf(AccessDeniedException.class);
        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    void nonexistentCompanyReturnsNotFound() {
        User recruiter = user(7L, "recruiter@ats.com", Role.RECRUITER);
        when(authentication.getName()).thenReturn(recruiter.getEmail());
        when(userRepository.findByEmailIgnoreCase(recruiter.getEmail())).thenReturn(Optional.of(recruiter));
        when(companyRepository.findById(15L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.create(request(15L, 7L), authentication))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Company not found: 15");
    }

    @Test
    void nonRecruiterCannotCreateJob() {
        User candidate = user(8L, "candidate@ats.com", Role.CANDIDATE);
        when(authentication.getName()).thenReturn(candidate.getEmail());
        when(userRepository.findByEmailIgnoreCase(candidate.getEmail())).thenReturn(Optional.of(candidate));

        assertThatThrownBy(() -> jobService.create(request(15L, 8L), authentication))
                .isInstanceOf(AccessDeniedException.class);
        verify(companyRepository, never()).findById(any());
    }

    private JobRequest request(Long companyId, Long recruiterId) {
        return new JobRequest("Backend Engineer", "Build backend services", null, null, null,
                null, null, null, null, null, null, null, null, null, null, companyId, recruiterId);
    }

    private Company company(Long id, User owner) {
        return Company.builder().id(id).name("Company " + id).owner(owner).build();
    }

    private User user(Long id, String email, Role role) {
        return User.builder().id(id).name(role.name()).email(email).password("unused")
                .role(role).status(UserStatus.ACTIVE).build();
    }
}