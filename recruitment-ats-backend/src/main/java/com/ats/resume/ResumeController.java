package com.ats.resume;

import com.ats.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
@Transactional
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping("/upload/{candidateUserId}")
    public ApiResponse<ResumeResponse> upload(
            @PathVariable Long candidateUserId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws Exception {
        return ApiResponse.ok("Resume uploaded and parsed",
            ResumeResponse.from(resumeService.upload(candidateUserId, file, authentication)));
    }
}
