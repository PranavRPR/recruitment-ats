package com.ats.ai;

public record AiMatchResponse(
        Long id,
        Long applicationId,
        double score,
        double skillScore,
        double experienceScore,
        double educationScore,
        double certificationScore,
        double roleScore,
        double semanticScore,
        String explanation) {

    public static AiMatchResponse from(AiMatchResult result) {
        return new AiMatchResponse(
                result.getId(),
                result.getApplication().getId(),
                result.getScore(),
                result.getSkillScore(),
                result.getExperienceScore(),
                result.getEducationScore(),
                result.getCertificationScore(),
                result.getRoleScore(),
                result.getSemanticScore(),
                result.getExplanation());
    }
}
