package com.ats.interview;

import com.ats.application.JobApplication;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "interviews")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Interview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private JobApplication application;

    @Enumerated(EnumType.STRING)
    private InterviewType type;

    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private String interviewers;
    private String meetingLink;

    @Column(length = 5000)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private InterviewStatus status = InterviewStatus.SCHEDULED;
}
