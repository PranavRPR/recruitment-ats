package com.ats.resume;

import java.time.LocalDateTime;

public record ResumeResponse(
        Long id,
        String originalFileName,
        String contentType,
        LocalDateTime uploadedAt,
        String parsedData,
        String extractedText) {

    public static ResumeResponse from(Resume resume) {
        return new ResumeResponse(
                resume.getId(),
                resume.getOriginalFileName(),
                resume.getContentType(),
                resume.getUploadedAt(),
                resume.getParsedData(),
                resume.getExtractedText());
    }
}
