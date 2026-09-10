package com.ats.ai;

import com.ats.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiMatchingService service;

    @PostMapping("/match/{applicationId}")
    public ApiResponse<AiMatchResponse> match(@PathVariable Long applicationId,
                                              Authentication authentication) {
        return ApiResponse.ok("AI candidate-job match calculated", service.match(applicationId, authentication));
    }

    @GetMapping("/match/{applicationId}")
    public ApiResponse<AiMatchResponse> get(@PathVariable Long applicationId,
                                            Authentication authentication) {
        return ApiResponse.ok("AI match loaded", service.get(applicationId, authentication));
    }
}
