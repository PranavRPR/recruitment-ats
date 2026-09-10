package com.ats.job;

import java.time.LocalDate;

public record JobResponse(
        Long id,
        String title,
        String description,
        String department,
        String location,
        EmploymentType employmentType,
        Integer experienceRequired,
        Double minSalary,
        Double maxSalary,
        String requiredSkills,
        String preferredSkills,
        String education,
        String responsibilities,
        String qualifications,
        LocalDate applicationDeadline,
        Integer openings,
        JobStatus status,
        CompanySummary company,
        UserSummary recruiter) {

    public record CompanySummary(Long id, String name, String industry, String location) { }

    public record UserSummary(Long id, String name, String email) { }

    public static JobResponse from(Job job) {
        return new JobResponse(
                job.getId(),
                job.getTitle(),
                job.getDescription(),
                job.getDepartment(),
                job.getLocation(),
                job.getEmploymentType(),
                job.getExperienceRequired(),
                job.getMinSalary(),
                job.getMaxSalary(),
                job.getRequiredSkills(),
                job.getPreferredSkills(),
                job.getEducation(),
                job.getResponsibilities(),
                job.getQualifications(),
                job.getApplicationDeadline(),
                job.getOpenings(),
                job.getStatus(),
                new CompanySummary(job.getCompany().getId(), job.getCompany().getName(),
                        job.getCompany().getIndustry(), job.getCompany().getLocation()),
                new UserSummary(job.getRecruiter().getId(), job.getRecruiter().getName(),
                        job.getRecruiter().getEmail()));
    }
}
