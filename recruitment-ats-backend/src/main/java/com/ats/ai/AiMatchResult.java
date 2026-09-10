package com.ats.ai;

import com.ats.application.JobApplication;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ai_match_results")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AiMatchResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private JobApplication application;

    private double score;
    private double skillScore;
    private double experienceScore;
    private double educationScore;
    private double certificationScore;
    private double roleScore;
    private double semanticScore;

    @Column(length = 10000)
    private String explanation;
}
