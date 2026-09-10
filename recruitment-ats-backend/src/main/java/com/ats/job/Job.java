package com.ats.job;

import com.ats.company.Company;
import com.ats.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "jobs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 10000, nullable = false)
    private String description;

    private String department;
    private String location;

    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType;

    private Integer experienceRequired;
    private Double minSalary;
    private Double maxSalary;

    @Column(length = 5000)
    private String requiredSkills;

    @Column(length = 5000)
    private String preferredSkills;

    private String education;
    private String responsibilities;
    private String qualifications;
    private LocalDate applicationDeadline;
    private Integer openings;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private JobStatus status = JobStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User recruiter;
}
