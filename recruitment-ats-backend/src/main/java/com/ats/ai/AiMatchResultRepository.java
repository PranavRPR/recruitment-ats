package com.ats.ai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiMatchResultRepository extends JpaRepository<AiMatchResult, Long> {
    Optional<AiMatchResult> findByApplicationId(Long applicationId);
}
