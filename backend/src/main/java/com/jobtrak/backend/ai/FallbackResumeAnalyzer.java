package com.jobtrak.backend.ai;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FallbackResumeAnalyzer {

	private static final Pattern WORD_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z+#.\\-]{2,}");
	private static final Set<String> STOP_WORDS = Set.of(
			"and", "the", "for", "with", "that", "this", "from", "into", "your", "you",
			"are", "will", "work", "team", "role", "job", "have", "has", "our", "their",
			"using", "use", "build", "able", "within", "across", "about", "experience"
	);

	public AiAnalysisResult analyze(String resumeText, String jobDescription) {
		List<String> jobKeywords = extractKeywords(jobDescription);
		String normalizedResume = resumeText.toLowerCase(Locale.ROOT);
		List<String> missingKeywords = jobKeywords.stream()
				.filter(keyword -> !normalizedResume.contains(keyword.toLowerCase(Locale.ROOT)))
				.limit(10)
				.toList();

		int matchedCount = Math.max(jobKeywords.size() - missingKeywords.size(), 0);
		int matchScore = jobKeywords.isEmpty() ? 50 : Math.min(95, Math.max(20, (matchedCount * 100) / jobKeywords.size()));
		List<String> topMissing = missingKeywords.stream().limit(3).toList();

		List<String> improvements = new ArrayList<>();
		improvements.add("Add measurable impact to your strongest experience bullets, such as scale, revenue, latency, users, or time saved.");
		improvements.add("Mirror the job description language where accurate, especially around: " + joinOrFallback(topMissing, "the role's core requirements") + ".");
		improvements.add("Move the most relevant technical skills and accomplishments closer to the top of the resume.");

		List<String> resumeRewritePlan = List.of(
				"Summary: add a 2-3 line target summary that names the role type and your strongest matching technical strengths.",
				"Skills: group tools and technologies by category so the job's required keywords are easy to scan.",
				"Experience or Projects: lead with the bullets that best prove the job description requirements, then add measurable impact where accurate."
		);

		List<String> bulletPlacementSuggestions = List.of(
				"Add the strongest rewritten bullet under the most relevant Experience role or Project that already proves that skill.",
				"Place technical implementation bullets under Projects if the experience came from academic, portfolio, or personal work.",
				"Put measurable outcome bullets near the top of each relevant role so recruiters see impact quickly."
		);

		List<String> suggestedSkills = missingKeywords.isEmpty()
				? jobKeywords.stream().limit(6).toList()
				: missingKeywords.stream().limit(6).toList();

		List<String> keywordPlacementSuggestions = suggestedSkills.stream()
				.map(skill -> "Add \"" + skill + "\" to Skills if accurate, then prove it in an Experience or Project bullet.")
				.toList();

		String coverLetter = """
				Dear Hiring Team,

				I am excited to apply for this role because my background aligns strongly with the needs described in the job posting. My experience can help your team deliver reliable, user-focused results while continuing to grow in the areas most important to this position.

				I would welcome the opportunity to discuss how my skills and projects connect to your team's goals.

				Sincerely,
				JobTrak Candidate
				""";

		return new AiAnalysisResult(
				matchScore,
				missingKeywords,
				improvements,
				resumeRewritePlan,
				bulletPlacementSuggestions,
				keywordPlacementSuggestions,
				suggestedSkills,
				coverLetter,
				"LOCAL_FALLBACK",
				"keyword-match-v1"
		);
	}

	private List<String> extractKeywords(String text) {
		Map<String, Integer> counts = new LinkedHashMap<>();
		Matcher matcher = WORD_PATTERN.matcher(text.toLowerCase(Locale.ROOT));

		while (matcher.find()) {
			String word = matcher.group();
			if (word.length() < 4 || STOP_WORDS.contains(word)) {
				continue;
			}
			counts.put(word, counts.getOrDefault(word, 0) + 1);
		}

		return counts.entrySet()
				.stream()
				.sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
				.map(Map.Entry::getKey)
				.limit(15)
				.toList();
	}

	private String joinOrFallback(List<String> values, String fallback) {
		if (values.isEmpty()) {
			return fallback;
		}
		return String.join(", ", values);
	}
}
