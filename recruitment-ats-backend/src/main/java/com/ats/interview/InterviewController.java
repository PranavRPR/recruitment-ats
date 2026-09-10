package com.ats.interview;

import com.ats.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
@Transactional
public class InterviewController {

    private final InterviewService service;

    @PostMapping
    public ApiResponse<InterviewResponse> create(@RequestBody InterviewRequest request,
                                         Authentication authentication) {
        return ApiResponse.ok("Interview scheduled", service.create(request, authentication));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<InterviewResponse> status(
            @PathVariable Long id,
            @RequestParam InterviewStatus status,
            Authentication authentication) {
        return ApiResponse.ok("Interview status updated", service.updateStatus(id, status, authentication));
    }

    @GetMapping
    public ApiResponse<List<InterviewResponse>> my(Authentication authentication) {
        return ApiResponse.ok("Interviews loaded", service.my(authentication));
    }

    @GetMapping("/my")
    public ApiResponse<List<InterviewResponse>> myCompatibility(Authentication authentication) {
        return ApiResponse.ok("Interviews loaded", service.my(authentication));
    }
}
