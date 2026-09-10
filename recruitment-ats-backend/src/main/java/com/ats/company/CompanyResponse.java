package com.ats.company;

import com.ats.user.Role;
import com.ats.user.UserStatus;

public record CompanyResponse(
        Long id,
        Long companyId,
        String name,
        String description,
        String website,
        String industry,
        String size,
        String location,
        OwnerSummary owner) {

    public record OwnerSummary(Long id, String name, String email, Role role, UserStatus status) { }

    public static CompanyResponse from(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getId(),
                company.getName(),
                company.getDescription(),
                company.getWebsite(),
                company.getIndustry(),
                company.getSize(),
                company.getLocation(),
                new OwnerSummary(
                        company.getOwner().getId(),
                        company.getOwner().getName(),
                        company.getOwner().getEmail(),
                        company.getOwner().getRole(),
                        company.getOwner().getStatus()));
    }
}