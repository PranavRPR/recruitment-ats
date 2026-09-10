package com.ats.candidate;

import com.ats.user.User;
import com.ats.user.UserRepository;
import com.ats.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CandidateProfileService {

    private final CandidateProfileRepository repository;
    private final UserRepository userRepository;

    @Transactional
    public CandidateProfile save(Long userId, CandidateProfileRequest r,
                                 Authentication authentication) {
        verifyOwner(userId, authentication);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found"));

        CandidateProfile profile = repository.findByUserId(userId)
                .orElse(CandidateProfile.builder().user(user).build());

        profile.setSummary(r.summary());
        profile.setPhone(r.phone());
        profile.setLocation(r.location());
        profile.setExperienceYears(r.experienceYears());
        profile.setSkills(r.skills());
        profile.setEducation(r.education());
        profile.setWorkExperience(r.workExperience());
        profile.setCertifications(r.certifications());
        profile.setProjects(r.projects());
        profile.setLanguages(r.languages());
        profile.setLinkedIn(r.linkedIn());
        profile.setPortfolio(r.portfolio());
        profile.setGithub(r.github());

        return repository.save(profile);
    }

    @Transactional(readOnly = true)
    public CandidateProfile get(Long userId, Authentication authentication) {
        verifyOwner(userId, authentication);
        return repository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));
    }

    private void verifyOwner(Long userId, Authentication authentication) {
        User currentUser = userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
        if (currentUser.getRole() != com.ats.user.Role.ADMIN
                && !currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("You can only access your own candidate profile");
        }
    }
}
