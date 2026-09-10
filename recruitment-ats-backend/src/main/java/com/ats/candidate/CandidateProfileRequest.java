package com.ats.candidate;

public record CandidateProfileRequest(
        String summary,
        String phone,
        String location,
        Integer experienceYears,
        String skills,
        String education,
        String workExperience,
        String certifications,
        String projects,
        String languages,
        String linkedIn,
        String portfolio,
        String github
) {}
