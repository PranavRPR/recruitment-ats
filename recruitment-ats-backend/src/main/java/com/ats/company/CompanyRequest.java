package com.ats.company;

import jakarta.validation.constraints.NotBlank;

public record CompanyRequest(
        @NotBlank String name,
        String description,
        String website,
        String industry,
        String size,
        String location
) {}
