package com.jobtrak.backend.dto;

import java.time.Instant;
import java.util.List;

public record AiAnalysisResponse(
		Long id,
		Long resumeId,
		Long applicationId,
		Integer matchScore,
		List<String> missingKeywords,
		List<String> resumeBulletImprovements,
		List<String> resumeRewritePlan,
		List<String> bulletPlacementSuggestions,
		List<String> keywordPlacementSuggestions,
		List<String> suggestedSkills,
		String coverLetter,
		String source,
		String model,
		Instant createdAt
) {
}
