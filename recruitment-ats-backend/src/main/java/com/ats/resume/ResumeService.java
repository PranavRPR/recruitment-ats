package com.ats.resume;

import com.ats.candidate.CandidateProfile;
import com.ats.candidate.CandidateProfileRepository;
import com.ats.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import com.ats.user.User;
import com.ats.user.UserRepository;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final CandidateProfileRepository candidateRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final Tika tika = new Tika();

    @Value("${app.upload.dir}")
    private String uploadDir;

    public Resume upload(Long candidateUserId, MultipartFile file, Authentication authentication) throws Exception {
        User currentUser = userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
        if (!currentUser.getId().equals(candidateUserId)
                || currentUser.getRole() != com.ats.user.Role.CANDIDATE) {
            throw new AccessDeniedException("You can only upload your own resume");
        }
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Resume file is required");
        if (file.getSize() > 10 * 1024 * 1024) throw new IllegalArgumentException("Resume must not exceed 10MB");

        byte[] content = file.getBytes();
        String detected = tika.detect(content);
        String original = Objects.requireNonNullElse(file.getOriginalFilename(), "resume");
        String lower = original.toLowerCase();

        if (!(lower.endsWith(".pdf") || lower.endsWith(".doc") || lower.endsWith(".docx"))) {
            throw new IllegalArgumentException("Unsupported file type. Only PDF, DOC and DOCX files are supported");
        }
        if (detected == null || (!detected.equals("application/pdf")
                && !detected.equals("application/msword")
                && !detected.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {
            throw new IllegalArgumentException("Unsupported resume content type");
        }

        CandidateProfile candidate = candidateRepository.findByUserId(candidateUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

        Path dir = Paths.get(uploadDir);
        Files.createDirectories(dir);

        String stored = UUID.randomUUID() + "-" + original.replaceAll("[^a-zA-Z0-9._-]", "_");
        Path destination = dir.resolve(stored);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

        String text = extractText(file, lower);

        Resume resume = Resume.builder()
                .candidate(candidate)
                .originalFileName(original)
                .storedFileName(stored)
                .filePath(destination.toAbsolutePath().toString())
                .contentType(detected)
                .uploadedAt(LocalDateTime.now())
                .extractedText(text)
                .parsedData(buildBasicParsedData(text))
                .build();

        return resumeRepository.save(resume);
    }

    private String extractText(MultipartFile file, String filename) throws Exception {
        if (filename.endsWith(".pdf")) {
            try (var document = Loader.loadPDF(file.getBytes())) {
                return new PDFTextStripper().getText(document);
            }
        }
        return tika.parseToString(file.getInputStream());
    }

    private String buildBasicParsedData(String text) {
        String normalized = text == null ? "" : text.toLowerCase();
        List<String> knownSkills = List.of(
                "java", "spring boot", "spring", "sql", "mysql", "postgresql",
                "javascript", "typescript", "react", "angular", "docker",
                "aws", "kubernetes", "python", "rest api", "maven", "git"
        );

        List<String> found = knownSkills.stream()
                .filter(s -> normalized.contains(s))
                .toList();

        return "{\"skills\":" + found.stream()
                .map(s -> "\"" + s + "\"")
                .toList() + "}";
    }
}
