package com.jobtrak.backend.dto;

public record AiAnalysisRequest(
		Long resumeId,
		Long applicationId,
		String resumeText,
		String jobDescription
) {
}
