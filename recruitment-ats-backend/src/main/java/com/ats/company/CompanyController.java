package com.ats.company;

import com.ats.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RestController
@RequestMapping("/api/recruiter/companies")
@RequiredArgsConstructor
@Transactional
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping("/{ownerId}")
    public ApiResponse<CompanyResponse> create(
            @PathVariable Long ownerId,
            @Valid @RequestBody CompanyRequest request,
            Authentication authentication) {
        return ApiResponse.ok("Company created",
                CompanyResponse.from(companyService.create(ownerId, request, authentication)));
    }

    @GetMapping
    public ApiResponse<List<CompanyResponse>> own(Authentication authentication) {
        return ApiResponse.ok("Companies loaded",
                companyService.findByAuthenticatedOwner(authentication).stream()
                        .map(CompanyResponse::from)
                        .toList());
    }

    @GetMapping("/owner/{ownerId}")
    public ApiResponse<List<CompanyResponse>> byOwner(@PathVariable Long ownerId,
                                                      Authentication authentication) {
        return ApiResponse.ok("Companies loaded",
                companyService.findByOwner(ownerId, authentication).stream()
                        .map(CompanyResponse::from)
                        .toList());
    }
}
