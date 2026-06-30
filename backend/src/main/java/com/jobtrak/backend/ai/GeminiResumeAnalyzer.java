package com.jobtrak.backend.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class GeminiResumeAnalyzer implements ResumeAnalyzer {

	private static final Logger logger = LoggerFactory.getLogger(GeminiResumeAnalyzer.class);

	private final RestClient restClient;
	private final ObjectMapper objectMapper;
	private final FallbackResumeAnalyzer fallbackResumeAnalyzer;
	private final String apiKey;
	private final String model;
	private final String fallbackModel;

	public GeminiResumeAnalyzer(
			FallbackResumeAnalyzer fallbackResumeAnalyzer,
			@Value("${app.gemini.api-key}") String apiKey,
			@Value("${app.gemini.model}") String model,
			@Value("${app.gemini.fallback-model}") String fallbackModel
	) {
		this.restClient = RestClient.builder()
				.baseUrl("https://generativelanguage.googleapis.com")
				.build();
		this.objectMapper = new ObjectMapper();
		this.fallbackResumeAnalyzer = fallbackResumeAnalyzer;
		this.apiKey = apiKey;
		this.model = model;
		this.fallbackModel = fallbackModel;
	}

	@Override
	public AiAnalysisResult analyze(String resumeText, String jobDescription) {
		if (apiKey == null || apiKey.isBlank()) {
			logger.warn("Gemini API key is not configured. Using local fallback analyzer.");
			return fallbackResumeAnalyzer.analyze(resumeText, jobDescription);
		}

		for (String modelName : configuredModels()) {
			String responseBody = null;

			try {
				logger.info("Sending resume analysis request to Gemini with model {}", modelName);
				responseBody = restClient.post()
						.uri("/v1beta/models/{model}:generateContent", modelName)
						.header("x-goog-api-key", apiKey)
						.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.body(buildRequestBody(resumeText, jobDescription))
						.retrieve()
						.body(String.class);

				AiAnalysisResult parsed = parseResponse(responseBody, modelName);
				logger.info("Gemini resume analysis completed successfully with model {}", modelName);
				return parsed;
			} catch (RestClientResponseException ex) {
				logger.warn(
						"Gemini request failed for model {} with HTTP {}. Response body: {}",
						modelName,
						ex.getStatusCode().value(),
						ex.getResponseBodyAsString()
				);
			} catch (RuntimeException ex) {
				logger.warn(
						"Gemini response from model {} could not be parsed. finishReason={}, outputLength={}.",
						modelName,
						extractFinishReason(responseBody),
						extractGeneratedTextLength(responseBody),
						ex
				);
			}
		}

		logger.warn("All configured Gemini models failed. Using local fallback analyzer.");
		return fallbackResumeAnalyzer.analyze(resumeText, jobDescription);
	}

	private List<String> configuredModels() {
		if (fallbackModel == null || fallbackModel.isBlank() || fallbackModel.equals(model)) {
			return List.of(model);
		}

		return List.of(model, fallbackModel);
	}

	private AiAnalysisResult toGeminiResult(AiAnalysisResult parsed, String modelName) {
		return new AiAnalysisResult(
				parsed.matchScore(),
				parsed.missingKeywords(),
				parsed.resumeBulletImprovements(),
				parsed.resumeRewritePlan(),
				parsed.bulletPlacementSuggestions(),
				parsed.keywordPlacementSuggestions(),
				parsed.suggestedSkills(),
				parsed.coverLetter(),
				"GEMINI",
				modelName
			);
	}

	private Map<String, Object> buildRequestBody(String resumeText, String jobDescription) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("contents", List.of(
				Map.of(
						"role", "user",
						"parts", List.of(Map.of("text", buildPrompt(resumeText, jobDescription)))
				)
		));
		body.put("generationConfig", Map.of(
				"temperature", 0.2,
				"maxOutputTokens", 8192,
				"responseMimeType", "application/json",
				"responseSchema", buildSchema()
		));
		return body;
	}

	private String buildPrompt(String resumeText, String jobDescription) {
		return """
				You are JobTrak's resume analysis engine.
				Act like a practical technical recruiter and resume coach.
				Ground every recommendation in the provided resume and job description.
				Do not invent employers, degrees, certifications, metrics, or tools that are not supported by the resume.
				Return only JSON that matches the schema.

				Analyze the resume against the job description and produce application-ready coaching.

				Scoring rules:
				- 90-100: strong direct match with clear evidence for most required skills
				- 70-89: good match with a few important gaps
				- 50-69: partial match that needs targeted edits before applying
				- below 50: weak match or too little resume evidence

				Output rules:
				- missingKeywords: include important job description terms that are missing or weakly represented in the resume
				- resumeBulletImprovements: write 4 concrete resume bullet rewrites and include exact placement in each item
				- Format every resumeBulletImprovements item like: "Project: NinerMine - Added bullet: Built ..."
				- Use section labels such as "Experience: Company Name", "Project: Project Name", "Skills", "Summary", "Education", or "Certifications"
				- resumeRewritePlan: explain how to improve the resume as a whole, section by section, in 4 concise items
				- bulletPlacementSuggestions: return an empty array because placement belongs inside resumeBulletImprovements
				- keywordPlacementSuggestions: for missing keywords and suggested skills, tell the candidate where each should naturally appear and why
				- suggestedSkills: include skills the candidate should emphasize based on both documents
				- coverLetter: write a concise, role-specific cover letter in 3-4 short paragraphs
				- Keep advice specific to this role. Avoid generic resume tips.
				- Keep every array item under 180 characters.
				- Return complete valid JSON only. Do not truncate the JSON.

				RESUME:
				%s

				JOB DESCRIPTION:
				%s
				""".formatted(resumeText, jobDescription);
	}

	private Map<String, Object> buildSchema() {
		return Map.of(
				"type", "object",
				"properties", Map.of(
						"matchScore", Map.of(
								"type", "integer",
								"description", "Overall resume-to-job match score from 0 to 100.",
								"minimum", 0,
								"maximum", 100
						),
						"missingKeywords", stringArraySchema("Important job description keywords or requirements missing or weak in the resume."),
						"resumeBulletImprovements", stringArraySchema("Concrete improved resume bullets with exact resume placement included in each item."),
						"resumeRewritePlan", stringArraySchema("Section-by-section plan for improving the resume as a whole."),
						"bulletPlacementSuggestions", stringArraySchema("Empty array. Bullet placement belongs inside resumeBulletImprovements."),
						"keywordPlacementSuggestions", stringArraySchema("Where missing keywords and suggested skills should be placed naturally in the resume."),
						"suggestedSkills", stringArraySchema("Skills the candidate should emphasize based on the resume and role."),
						"coverLetter", Map.of(
								"type", "string",
								"description", "A concise tailored cover letter for this job."
						)
				),
				"required", List.of(
						"matchScore",
						"missingKeywords",
						"resumeBulletImprovements",
						"resumeRewritePlan",
						"bulletPlacementSuggestions",
						"keywordPlacementSuggestions",
						"suggestedSkills",
						"coverLetter"
				)
		);
	}

	private Map<String, Object> stringArraySchema(String description) {
		return Map.of(
				"type", "array",
				"description", description,
				"items", Map.of("type", "string")
		);
	}

	private AiAnalysisResult parseResponse(String responseBody, String modelName) {
		JsonNode response;

		try {
			response = objectMapper.readTree(responseBody);
		} catch (JsonProcessingException ex) {
			throw new IllegalStateException("Gemini returned response JSON in an unexpected format", ex);
		}

		String outputText = extractOutputText(response);

		try {
			ModelAnalysisResult result = objectMapper.readValue(outputText, ModelAnalysisResult.class);
			return toGeminiResult(new AiAnalysisResult(
					result.matchScore(),
					result.missingKeywords(),
					result.resumeBulletImprovements(),
					result.resumeRewritePlan(),
					result.bulletPlacementSuggestions(),
					result.keywordPlacementSuggestions(),
					result.suggestedSkills(),
					result.coverLetter(),
					"GEMINI",
					modelName
			), modelName);
		} catch (JsonProcessingException ex) {
			throw new IllegalStateException("Gemini returned analysis in an unexpected format", ex);
		}
	}

	private String extractOutputText(JsonNode response) {
		if (response == null) {
			throw new IllegalStateException("Gemini returned an empty response");
		}

		JsonNode candidates = response.get("candidates");
		if (candidates != null && candidates.isArray() && !candidates.isEmpty()) {
			JsonNode parts = candidates.get(0).path("content").path("parts");
			if (parts.isArray() && !parts.isEmpty()) {
				JsonNode text = parts.get(0).get("text");
				if (text != null && text.isTextual()) {
					return text.asText();
				}
			}
		}

		throw new IllegalStateException("Gemini response did not contain generated text");
	}

	private String extractFinishReason(String responseBody) {
		if (responseBody == null || responseBody.isBlank()) {
			return "empty";
		}

		try {
			JsonNode response = objectMapper.readTree(responseBody);
			return response.path("candidates").path(0).path("finishReason").asText("unknown");
		} catch (JsonProcessingException ex) {
			return "unreadable";
		}
	}

	private int extractGeneratedTextLength(String responseBody) {
		if (responseBody == null || responseBody.isBlank()) {
			return 0;
		}

		try {
			JsonNode response = objectMapper.readTree(responseBody);
			JsonNode text = response.path("candidates").path(0).path("content").path("parts").path(0).path("text");
			return text.isTextual() ? text.asText().length() : 0;
		} catch (JsonProcessingException ex) {
			return 0;
		}
	}

	private record ModelAnalysisResult(
			Integer matchScore,
			List<String> missingKeywords,
			List<String> resumeBulletImprovements,
			List<String> resumeRewritePlan,
			List<String> bulletPlacementSuggestions,
			List<String> keywordPlacementSuggestions,
			List<String> suggestedSkills,
			String coverLetter
	) {
	}
}
