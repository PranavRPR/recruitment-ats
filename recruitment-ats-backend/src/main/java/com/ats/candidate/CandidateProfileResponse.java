package com.ats.candidate;

public record CandidateProfileResponse(
        Long id,
        Long userId,
        String name,
        String email,
        String summary,
        String phone,
        String location,
        String profilePhoto,
        Integer experienceYears,
        String skills,
        String education,
        String workExperience,
        String certifications,
        String projects,
        String languages,
        String linkedIn,
        String portfolio,
        String github) {

    public static CandidateProfileResponse from(CandidateProfile profile) {
        var user = profile.getUser();
        return new CandidateProfileResponse(
                profile.getId(), user.getId(), user.getName(), user.getEmail(),
                profile.getSummary(), profile.getPhone(), profile.getLocation(), profile.getProfilePhoto(),
                profile.getExperienceYears(), profile.getSkills(), profile.getEducation(),
                profile.getWorkExperience(), profile.getCertifications(), profile.getProjects(),
                profile.getLanguages(), profile.getLinkedIn(), profile.getPortfolio(), profile.getGithub());
    }
}
