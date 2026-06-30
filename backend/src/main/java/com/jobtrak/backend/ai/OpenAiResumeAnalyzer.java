package com.jobtrak.backend.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class OpenAiResumeAnalyzer implements ResumeAnalyzer {

	private final RestClient restClient;
	private final ObjectMapper objectMapper;
	private final FallbackResumeAnalyzer fallbackResumeAnalyzer;
	private final String apiKey;
	private final String model;

	public OpenAiResumeAnalyzer(
			FallbackResumeAnalyzer fallbackResumeAnalyzer,
			@Value("${app.openai.api-key}") String apiKey,
			@Value("${app.openai.model}") String model
	) {
		this.restClient = RestClient.builder()
				.baseUrl("https://api.openai.com")
				.build();
		this.objectMapper = new ObjectMapper();
		this.fallbackResumeAnalyzer = fallbackResumeAnalyzer;
		this.apiKey = apiKey;
		this.model = model;
	}

	@Override
	public AiAnalysisResult analyze(String resumeText, String jobDescription) {
		if (apiKey == null || apiKey.isBlank()) {
			return fallbackResumeAnalyzer.analyze(resumeText, jobDescription);
		}

		JsonNode response = restClient.post()
				.uri("/v1/responses")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
				.contentType(MediaType.APPLICATION_JSON)
				.body(buildRequestBody(resumeText, jobDescription))
				.retrieve()
				.body(JsonNode.class);

		AiAnalysisResult parsed = parseResponse(response);
		return new AiAnalysisResult(
				parsed.matchScore(),
				parsed.missingKeywords(),
				parsed.resumeBulletImprovements(),
				parsed.resumeRewritePlan(),
				parsed.bulletPlacementSuggestions(),
				parsed.keywordPlacementSuggestions(),
				parsed.suggestedSkills(),
				parsed.coverLetter(),
				"OPENAI",
				model
		);
	}

	private Map<String, Object> buildRequestBody(String resumeText, String jobDescription) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("model", model);
		body.put("store", false);
		body.put("max_output_tokens", 2200);
		body.put("temperature", 0.2);
		body.put("input", List.of(
				Map.of(
						"role", "developer",
						"content", buildDeveloperInstructions()
				),
				Map.of(
						"role", "user",
						"content", buildPrompt(resumeText, jobDescription)
				)
		));
		body.put("text", Map.of("format", buildSchema()));
		return body;
	}

	private String buildDeveloperInstructions() {
		return """
				You are JobTrak's resume analysis engine.
				Act like a practical technical recruiter and resume coach.
				Ground every recommendation in the provided resume and job description.
				Do not invent employers, degrees, certifications, metrics, or tools that are not supported by the resume.
				Return only JSON that matches the schema.
				""";
	}

	private String buildPrompt(String resumeText, String jobDescription) {
		return """
				Analyze the resume against the job description and produce application-ready coaching.

				Scoring rules:
				- 90-100: strong direct match with clear evidence for most required skills
				- 70-89: good match with a few important gaps
				- 50-69: partial match that needs targeted edits before applying
				- below 50: weak match or too little resume evidence

				Output rules:
				- missingKeywords: include important job description terms that are missing or weakly represented in the resume
				- resumeBulletImprovements: write 4-6 concrete resume bullet rewrites using action verbs and measurable impact placeholders only when the resume supports them
				- resumeRewritePlan: explain how to improve the resume as a whole, section by section
				- bulletPlacementSuggestions: for each improved bullet, tell the candidate exactly where to add it, such as Summary, Skills, Experience under a specific employer, Projects, Education, or Certifications
				- keywordPlacementSuggestions: for missing keywords and suggested skills, tell the candidate where each should naturally appear and why
				- suggestedSkills: include skills the candidate should emphasize based on both documents
				- coverLetter: write a concise, role-specific cover letter in 3-4 short paragraphs
				- Keep advice specific to this role. Avoid generic resume tips.

				RESUME:
				%s

				JOB DESCRIPTION:
				%s
				""".formatted(resumeText, jobDescription);
	}

	private Map<String, Object> buildSchema() {
		return Map.of(
				"type", "json_schema",
				"name", "jobtrak_resume_analysis",
				"strict", true,
				"schema", Map.of(
						"type", "object",
						"additionalProperties", false,
						"required", List.of(
								"matchScore",
								"missingKeywords",
								"resumeBulletImprovements",
								"resumeRewritePlan",
								"bulletPlacementSuggestions",
								"keywordPlacementSuggestions",
								"suggestedSkills",
								"coverLetter"
						),
						"properties", Map.of(
								"matchScore", Map.of(
										"type", "integer",
										"description", "Overall resume-to-job match score from 0 to 100.",
										"minimum", 0,
										"maximum", 100
								),
								"missingKeywords", stringArraySchema("Important job description keywords or requirements missing or weak in the resume."),
								"resumeBulletImprovements", stringArraySchema("Concrete improved resume bullets tailored to the job description."),
								"resumeRewritePlan", stringArraySchema("Section-by-section plan for improving the resume as a whole."),
								"bulletPlacementSuggestions", stringArraySchema("Where each improved bullet should be added in the resume."),
								"keywordPlacementSuggestions", stringArraySchema("Where missing keywords and suggested skills should be placed naturally in the resume."),
								"suggestedSkills", stringArraySchema("Skills the candidate should emphasize based on the resume and role."),
								"coverLetter", Map.of(
										"type", "string",
										"description", "A concise tailored cover letter for this job."
								)
						)
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

	private AiAnalysisResult parseResponse(JsonNode response) {
		String outputText = extractOutputText(response);

		try {
			ModelAnalysisResult result = objectMapper.readValue(outputText, ModelAnalysisResult.class);
			return new AiAnalysisResult(
					result.matchScore(),
					result.missingKeywords(),
					result.resumeBulletImprovements(),
					result.resumeRewritePlan(),
					result.bulletPlacementSuggestions(),
					result.keywordPlacementSuggestions(),
					result.suggestedSkills(),
					result.coverLetter(),
					"OPENAI",
					model
			);
		} catch (JsonProcessingException ex) {
			throw new IllegalStateException("OpenAI returned analysis in an unexpected format", ex);
		}
	}

	private String extractOutputText(JsonNode response) {
		if (response == null) {
			throw new IllegalStateException("OpenAI returned an empty response");
		}

		JsonNode outputText = response.get("output_text");
		if (outputText != null && outputText.isTextual()) {
			return outputText.asText();
		}

		StringBuilder builder = new StringBuilder();
		JsonNode output = response.get("output");
		if (output != null && output.isArray()) {
			for (JsonNode item : output) {
				JsonNode content = item.get("content");
				if (content == null || !content.isArray()) {
					continue;
				}
				for (JsonNode contentItem : content) {
					JsonNode text = contentItem.get("text");
					if (text != null && text.isTextual()) {
						builder.append(text.asText());
					}
				}
			}
		}

		if (builder.isEmpty()) {
			throw new IllegalStateException("OpenAI response did not contain output text");
		}

		return builder.toString();
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
