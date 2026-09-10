package com.ats.job;

import com.ats.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobController {

    private final JobService jobService;

    @GetMapping
    public ApiResponse<List<JobResponse>> publishedJobs() {
        return ApiResponse.ok("Jobs loaded", jobService.published().stream().map(JobResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ApiResponse<JobResponse> get(@PathVariable Long id) {
        return ApiResponse.ok("Job loaded", JobResponse.from(jobService.get(id)));
    }

    @PostMapping
    @Transactional
    public ApiResponse<JobResponse> create(@Valid @RequestBody JobRequest request,
                                           Authentication authentication) {
        return ApiResponse.ok("Job created", JobResponse.from(jobService.create(request, authentication)));
    }

    @PatchMapping("/{id}/status")
    @Transactional
    public ApiResponse<JobResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam JobStatus status,
            Authentication authentication) {
        return ApiResponse.ok("Job status updated",
                JobResponse.from(jobService.updateStatus(id, status, authentication)));
    }

    @GetMapping("/recruiter/{recruiterId}")
    public ApiResponse<List<JobResponse>> byRecruiter(@PathVariable Long recruiterId,
                                                      Authentication authentication) {
        return ApiResponse.ok("Recruiter jobs loaded",
                jobService.byRecruiter(recruiterId, authentication).stream().map(JobResponse::from).toList());
    }
}
