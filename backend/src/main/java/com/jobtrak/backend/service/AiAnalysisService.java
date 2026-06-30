package com.jobtrak.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobtrak.backend.ai.AiAnalysisResult;
import com.jobtrak.backend.ai.ResumeAnalyzer;
import com.jobtrak.backend.dto.AiAnalysisRequest;
import com.jobtrak.backend.dto.AiAnalysisResponse;
import com.jobtrak.backend.entity.AiAnalysis;
import com.jobtrak.backend.entity.JobApplication;
import com.jobtrak.backend.entity.Resume;
import com.jobtrak.backend.entity.User;
import com.jobtrak.backend.repository.AiAnalysisRepository;
import com.jobtrak.backend.repository.JobApplicationRepository;
import com.jobtrak.backend.repository.ResumeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AiAnalysisService {

	private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
	};

	private final AiAnalysisRepository aiAnalysisRepository;
	private final ResumeRepository resumeRepository;
	private final JobApplicationRepository jobApplicationRepository;
	private final UserLookupService userLookupService;
	private final ResumeAnalyzer resumeAnalyzer;
	private final ObjectMapper objectMapper;

	public AiAnalysisService(
			AiAnalysisRepository aiAnalysisRepository,
			ResumeRepository resumeRepository,
			JobApplicationRepository jobApplicationRepository,
			UserLookupService userLookupService,
			ResumeAnalyzer resumeAnalyzer
	) {
		this.aiAnalysisRepository = aiAnalysisRepository;
		this.resumeRepository = resumeRepository;
		this.jobApplicationRepository = jobApplicationRepository;
		this.userLookupService = userLookupService;
		this.resumeAnalyzer = resumeAnalyzer;
		this.objectMapper = new ObjectMapper();
	}

	public AiAnalysisResponse analyze(String email, AiAnalysisRequest request) {
		User user = userLookupService.getByEmail(email);
		Resume resume = findResume(user, request.resumeId());
		JobApplication application = findApplication(user, request.applicationId());
		String resumeText = resolveResumeText(request, resume);
		String jobDescription = resolveJobDescription(request, application);
		AiAnalysisResult result = resumeAnalyzer.analyze(resumeText, jobDescription);

		AiAnalysis analysis = new AiAnalysis(
				user,
				resume,
				application,
				resumeText,
				jobDescription,
				result.matchScore(),
				toJson(result.missingKeywords()),
				toJson(result.resumeBulletImprovements()),
				toJson(result.resumeRewritePlan()),
				toJson(result.bulletPlacementSuggestions()),
				toJson(result.keywordPlacementSuggestions()),
				toJson(result.suggestedSkills()),
				result.coverLetter(),
				result.source(),
				result.model()
		);

		AiAnalysis savedAnalysis = aiAnalysisRepository.save(analysis);
		pruneAnalysisHistory(user);
		return toResponse(savedAnalysis);
	}

	public List<AiAnalysisResponse> getAll(String email) {
		User user = userLookupService.getByEmail(email);
		return aiAnalysisRepository.findByUserOrderByCreatedAtDesc(user)
				.stream()
				.limit(10)
				.map(this::toResponse)
				.toList();
	}

	public AiAnalysisResponse getById(String email, Long id) {
		User user = userLookupService.getByEmail(email);
		AiAnalysis analysis = aiAnalysisRepository.findByIdAndUser(id, user)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "AI analysis not found"));
		return toResponse(analysis);
	}

	private Resume findResume(User user, Long resumeId) {
		if (resumeId == null) {
			return null;
		}
		return resumeRepository.findByIdAndUser(resumeId, user)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume not found"));
	}

	private JobApplication findApplication(User user, Long applicationId) {
		if (applicationId == null) {
			return null;
		}
		return jobApplicationRepository.findByIdAndUser(applicationId, user)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
	}

	private String resolveResumeText(AiAnalysisRequest request, Resume resume) {
		if (resume != null) {
			return resume.getContent();
		}
		if (request.resumeText() == null || request.resumeText().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide resumeId or resumeText");
		}
		return request.resumeText().trim();
	}

	private String resolveJobDescription(AiAnalysisRequest request, JobApplication application) {
		if (application != null && application.getJobDescription() != null && !application.getJobDescription().isBlank()) {
			return application.getJobDescription();
		}
		if (request.jobDescription() == null || request.jobDescription().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide applicationId with a job description or jobDescription");
		}
		return request.jobDescription().trim();
	}

	private String toJson(List<String> values) {
		try {
			return objectMapper.writeValueAsString(values);
		} catch (JsonProcessingException ex) {
			throw new IllegalStateException("Unable to serialize AI analysis data", ex);
		}
	}

	private List<String> fromJson(String json) {
		if (json == null || json.isBlank()) {
			return List.of();
		}

		try {
			return objectMapper.readValue(json, STRING_LIST_TYPE);
		} catch (JsonProcessingException ex) {
			throw new IllegalStateException("Unable to read AI analysis data", ex);
		}
	}

	private void pruneAnalysisHistory(User user) {
		List<AiAnalysis> analyses = aiAnalysisRepository.findByUserOrderByCreatedAtDesc(user);

		if (analyses.size() <= 10) {
			return;
		}

		aiAnalysisRepository.deleteAll(analyses.subList(10, analyses.size()));
	}

	private AiAnalysisResponse toResponse(AiAnalysis analysis) {
		Long resumeId = analysis.getResume() == null ? null : analysis.getResume().getId();
		Long applicationId = analysis.getJobApplication() == null ? null : analysis.getJobApplication().getId();

		return new AiAnalysisResponse(
				analysis.getId(),
				resumeId,
				applicationId,
				analysis.getMatchScore(),
				fromJson(analysis.getMissingKeywordsJson()),
				fromJson(analysis.getResumeBulletImprovementsJson()),
				fromJson(analysis.getResumeRewritePlanJson()),
				fromJson(analysis.getBulletPlacementSuggestionsJson()),
				fromJson(analysis.getKeywordPlacementSuggestionsJson()),
				fromJson(analysis.getSuggestedSkillsJson()),
				analysis.getCoverLetter(),
				analysis.getSource(),
				analysis.getModel(),
				analysis.getCreatedAt()
		);
	}
}
