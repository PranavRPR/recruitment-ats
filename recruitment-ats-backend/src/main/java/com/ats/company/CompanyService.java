package com.ats.company;

import com.ats.user.User;
import com.ats.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public Company create(Long ownerId, CompanyRequest request, Authentication authentication) {
        User currentUser = currentUser(authentication);
        if (currentUser.getRole() != com.ats.user.Role.RECRUITER) {
            throw new AccessDeniedException("Only recruiters can create companies");
        }
        if (!currentUser.getId().equals(ownerId)) {
            throw new AccessDeniedException("You can only create companies for yourself");
        }

        return companyRepository.save(Company.builder()
                .name(request.name())
                .description(request.description())
                .website(request.website())
                .industry(request.industry())
                .size(request.size())
                .location(request.location())
                .owner(currentUser)
                .build());
    }

    public List<Company> findByOwner(Long ownerId, Authentication authentication) {
        User currentUser = currentUser(authentication);
        if (currentUser.getRole() != com.ats.user.Role.ADMIN
                && !currentUser.getId().equals(ownerId)) {
            throw new AccessDeniedException("You can only view your own companies");
        }
        return companyRepository.findByOwnerId(ownerId);
    }

    public List<Company> findByAuthenticatedOwner(Authentication authentication) {
        User currentUser = currentUser(authentication);
        return companyRepository.findByOwnerId(currentUser.getId());
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
    }
}
