package com.ats.interview;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
    List<Interview> findByApplicationCandidateUserId(Long userId);
    List<Interview> findByApplicationJobRecruiterId(Long recruiterId);
    List<Interview> findByApplicationJobCompanyOwnerId(Long ownerId);
}
