package com.ats.resume;

import com.ats.candidate.CandidateProfile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "resumes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Resume {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private CandidateProfile candidate;

    private String originalFileName;
    private String storedFileName;
    private String filePath;
    private String contentType;
    private LocalDateTime uploadedAt;

    @Column(columnDefinition = "TEXT")
    private String extractedText;

    @Column(columnDefinition = "TEXT")
    private String parsedData;
}
