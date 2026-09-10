package com.ats.application;

import com.ats.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Transactional
public class ApplicationController {

    private final ApplicationService service;

    @PostMapping("/apply/{jobId}/{candidateUserId}")
    public ApiResponse<ApplicationResponse> apply(
            @PathVariable Long jobId,
            @PathVariable Long candidateUserId,
            Authentication authentication) {
                return ApiResponse.ok("Application submitted",
                    ApplicationResponse.from(service.apply(jobId, candidateUserId, authentication)));
    }

    @GetMapping("/candidate/{userId}")
    public ApiResponse<List<ApplicationResponse>> byCandidate(
            @PathVariable Long userId,
            Authentication authentication) {
                return ApiResponse.ok("Applications loaded",
                    service.byCandidate(userId, authentication).stream().map(ApplicationResponse::from).toList());
    }

    @GetMapping("/job/{jobId}")
        public ApiResponse<List<ApplicationResponse>> byJob(
            @PathVariable Long jobId, Authentication authentication) {
        return ApiResponse.ok("Applications loaded", service.byJob(jobId, authentication).stream().map(ApplicationResponse::from).toList());
    }

    @PatchMapping("/{id}/status")
        public ApiResponse<ApplicationResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam ApplicationStatus status,
            Authentication authentication) {
            return ApiResponse.ok("Application status updated",
                ApplicationResponse.from(service.updateStatus(id, status, authentication)));
    }
}
