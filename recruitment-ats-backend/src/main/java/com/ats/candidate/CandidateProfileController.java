package com.ats.candidate;

import com.ats.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/candidate/profile")
@RequiredArgsConstructor
@Transactional
public class CandidateProfileController {

    private final CandidateProfileService service;

    @PutMapping("/{userId}")
    public ApiResponse<CandidateProfileResponse> save(
            @PathVariable Long userId,
            @RequestBody @Valid CandidateProfileRequest request,
            Authentication authentication) {
        return ApiResponse.ok("Profile saved", CandidateProfileResponse.from(service.save(userId, request, authentication)));
    }

    @GetMapping("/{userId}")
    public ApiResponse<CandidateProfileResponse> get(
            @PathVariable Long userId,
            Authentication authentication) {
        return ApiResponse.ok("Profile loaded", CandidateProfileResponse.from(service.get(userId, authentication)));
    }
}
