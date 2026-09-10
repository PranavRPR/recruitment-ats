package com.ats.company;

import com.ats.user.Role;
import com.ats.user.User;
import com.ats.user.UserRepository;
import com.ats.user.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CompanyService companyService;

    @Test
    void recruiterCreatesCompanyOwnedByAuthenticatedUser() {
        User recruiter = user(7L, "recruiter@ats.com", Role.RECRUITER);
        when(authentication.getName()).thenReturn(recruiter.getEmail());
        when(userRepository.findByEmailIgnoreCase(recruiter.getEmail())).thenReturn(Optional.of(recruiter));
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> {
            Company company = invocation.getArgument(0);
            company.setId(15L);
            return company;
        });

        Company result = companyService.create(7L, new CompanyRequest("ABC Technologies", null,
                null, null, null, null), authentication);

        assertThat(result.getId()).isEqualTo(15L);
        assertThat(result.getOwner()).isSameAs(recruiter);
        ArgumentCaptor<Company> captor = ArgumentCaptor.forClass(Company.class);
        verify(companyRepository).save(captor.capture());
        assertThat(captor.getValue().getOwner()).isSameAs(recruiter);
    }

    @Test
    void nonRecruiterCannotCreateCompany() {
        User candidate = user(8L, "candidate@ats.com", Role.CANDIDATE);
        when(authentication.getName()).thenReturn(candidate.getEmail());
        when(userRepository.findByEmailIgnoreCase(candidate.getEmail())).thenReturn(Optional.of(candidate));

        assertThatThrownBy(() -> companyService.create(8L, new CompanyRequest("ABC Technologies", null,
                null, null, null, null), authentication))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void recruiterCannotCreateCompanyForAnotherUser() {
        User recruiter = user(7L, "recruiter@ats.com", Role.RECRUITER);
        when(authentication.getName()).thenReturn(recruiter.getEmail());
        when(userRepository.findByEmailIgnoreCase(recruiter.getEmail())).thenReturn(Optional.of(recruiter));

        assertThatThrownBy(() -> companyService.create(9L, new CompanyRequest("ABC Technologies", null,
                null, null, null, null), authentication))
                .isInstanceOf(AccessDeniedException.class);
    }

    private User user(Long id, String email, Role role) {
        return User.builder().id(id).name(role.name()).email(email).password("unused")
                .role(role).status(UserStatus.ACTIVE).build();
    }
}