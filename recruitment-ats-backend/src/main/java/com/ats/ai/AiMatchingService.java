package com.ats.ai;

import com.ats.application.*;
import com.ats.candidate.CandidateProfile;
import com.ats.job.Job;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import com.ats.user.Role;
import com.ats.user.User;
import com.ats.user.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiMatchingService {

    private final JobApplicationRepository applicationRepository;
    private final AiMatchResultRepository resultRepository;
    private final UserRepository userRepository;

    @Transactional
    public AiMatchResponse match(Long applicationId, Authentication authentication) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        verifyAccess(application, authentication);

        Job job = application.getJob();
        CandidateProfile candidate = application.getCandidate();

        double skills = skillScore(job.getRequiredSkills(), candidate.getSkills());
        double experience = experienceScore(job.getExperienceRequired(), candidate.getExperienceYears());
        double education = textScore(job.getEducation(), candidate.getEducation());
        double certification = textScore(job.getPreferredSkills(), candidate.getCertifications());
        double role = textScore(job.getTitle(), candidate.getWorkExperience());
        double semantic = textScore(job.getDescription(), candidate.getSummary());

        double finalScore =
                skills * 0.40 +
                experience * 0.25 +
                education * 0.10 +
                certification * 0.05 +
                role * 0.10 +
                semantic * 0.10;

        String explanation = buildExplanation(job, candidate, skills, experience, finalScore);

        AiMatchResult result = resultRepository.findByApplicationId(applicationId)
                .orElse(AiMatchResult.builder().application(application).build());

        result.setScore(round(finalScore));
        result.setSkillScore(round(skills));
        result.setExperienceScore(round(experience));
        result.setEducationScore(round(education));
        result.setCertificationScore(round(certification));
        result.setRoleScore(round(role));
        result.setSemanticScore(round(semantic));
        result.setExplanation(explanation);

        return AiMatchResponse.from(resultRepository.save(result));
    }

    @Transactional(readOnly = true)
    public AiMatchResponse get(Long applicationId, Authentication authentication) {
        AiMatchResult result = resultRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("AI match result not found"));
        verifyAccess(result.getApplication(), authentication);
        return AiMatchResponse.from(result);
    }

    private void verifyAccess(JobApplication application, Authentication authentication) {
        User user = userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
        if (user.getRole() == Role.ADMIN) return;
        boolean candidate = user.getRole() == Role.CANDIDATE
                && user.getId().equals(application.getCandidate().getUser().getId());
        boolean staff = (user.getRole() == Role.RECRUITER || user.getRole() == Role.HIRING_MANAGER)
                && user.getId().equals(application.getJob().getRecruiter().getId());
        if (!candidate && !staff) throw new AccessDeniedException("You are not authorized to view this match");
    }

    private double skillScore(String required, String candidate) {
        Set<String> requiredSet = csv(required);
        if (requiredSet.isEmpty()) return 100;
        Set<String> candidateSet = csv(candidate);
        long matched = requiredSet.stream()
                .filter(candidateSet::contains)
                .count();
        return matched * 100.0 / requiredSet.size();
    }

    private double experienceScore(Integer required, Integer actual) {
        if (required == null || required <= 0) return 100;
        if (actual == null || actual <= 0) return 0;
        return Math.min(100, actual * 100.0 / required);
    }

    private double textScore(String required, String candidate) {
        if (required == null || required.isBlank()) return 100;
        if (candidate == null || candidate.isBlank()) return 0;

        Set<String> requiredWords = words(required);
        Set<String> candidateWords = words(candidate);

        if (requiredWords.isEmpty()) return 100;

        long matched = requiredWords.stream().filter(candidateWords::contains).count();
        return matched * 100.0 / requiredWords.size();
    }

    private Set<String> csv(String value) {
        if (value == null || value.isBlank()) return Set.of();
        return Arrays.stream(value.toLowerCase().split("[,;|]"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());
    }

    private Set<String> words(String value) {
        return Arrays.stream(value.toLowerCase().replaceAll("[^a-z0-9+#. ]", " ").split("\\s+"))
                .filter(s -> s.length() > 2)
                .collect(Collectors.toSet());
    }

    private String buildExplanation(Job job, CandidateProfile candidate,
                                    double skills, double experience, double score) {
        return String.format(
                "AI decision-support result. Overall %.1f%%. Skills %.1f%%, experience %.1f%%. " +
                "This score is based only on job/candidate information and must not be treated as an automatic hiring decision.",
                score, skills, experience);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
