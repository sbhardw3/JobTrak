package com.jobtrak.backend.ai;

import java.util.List;

public record AiAnalysisResult(
		Integer matchScore,
		List<String> missingKeywords,
		List<String> resumeBulletImprovements,
		List<String> resumeRewritePlan,
		List<String> bulletPlacementSuggestions,
		List<String> keywordPlacementSuggestions,
		List<String> suggestedSkills,
		String coverLetter,
		String source,
		String model
) {
}
