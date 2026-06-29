package com.jobtrak.backend.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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
				parsed.suggestedSkills(),
				parsed.coverLetter(),
				"OPENAI",
				model
		);
	}

	private Map<String, Object> buildRequestBody(String resumeText, String jobDescription) {
		return Map.of(
				"model", model,
				"input", List.of(
						Map.of(
								"role", "developer",
								"content", "You are JobTrak's resume analysis engine. Return only data that matches the schema."
						),
						Map.of(
								"role", "user",
								"content", buildPrompt(resumeText, jobDescription)
						)
				),
				"text", Map.of("format", buildSchema())
		);
	}

	private String buildPrompt(String resumeText, String jobDescription) {
		return """
				Analyze this resume against the job description.

				Return:
				- matchScore from 0 to 100
				- missingKeywords from the job description
				- resumeBulletImprovements as concrete improved bullet suggestions
				- suggestedSkills the candidate should emphasize
				- coverLetter tailored to the role

				Resume:
				%s

				Job Description:
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
								"suggestedSkills",
								"coverLetter"
						),
						"properties", Map.of(
								"matchScore", Map.of(
										"type", "integer",
										"minimum", 0,
										"maximum", 100
								),
								"missingKeywords", stringArraySchema(),
								"resumeBulletImprovements", stringArraySchema(),
								"suggestedSkills", stringArraySchema(),
								"coverLetter", Map.of("type", "string")
						)
				)
		);
	}

	private Map<String, Object> stringArraySchema() {
		return Map.of(
				"type", "array",
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
			List<String> suggestedSkills,
			String coverLetter
	) {
	}
}
