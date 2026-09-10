package com.ats.job;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record JobRequest(
        @NotBlank String title,
        @NotBlank String description,
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
        @NotNull Long companyId,
        @NotNull Long recruiterId
) {}
