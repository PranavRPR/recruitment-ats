package com.ats.candidate;

import com.ats.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "candidate_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CandidateProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(length = 5000)
    private String summary;

    private String phone;
    private String location;
    private String profilePhoto;
    private Integer experienceYears;

    @Column(length = 5000)
    private String skills;

    @Column(length = 5000)
    private String education;

    @Column(length = 5000)
    private String workExperience;

    @Column(length = 3000)
    private String certifications;

    @Column(length = 5000)
    private String projects;

    private String languages;
    private String linkedIn;
    private String portfolio;
    private String github;
}
