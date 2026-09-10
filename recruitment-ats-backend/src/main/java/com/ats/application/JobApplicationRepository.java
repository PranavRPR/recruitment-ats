package com.ats.application;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByCandidateUserId(Long userId);
    List<JobApplication> findByJobId(Long jobId);
    boolean existsByJobIdAndCandidateUserId(Long jobId, Long userId);
}
