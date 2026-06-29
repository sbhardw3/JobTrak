package com.jobtrak.backend.ai;

public interface ResumeAnalyzer {

	AiAnalysisResult analyze(String resumeText, String jobDescription);
}
