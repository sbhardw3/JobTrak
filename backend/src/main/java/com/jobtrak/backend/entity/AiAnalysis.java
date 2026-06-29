package com.jobtrak.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "ai_analyses")
public class AiAnalysis {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "resume_id")
	private Resume resume;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "job_application_id")
	private JobApplication jobApplication;

	@Column(nullable = false, columnDefinition = "text")
	private String resumeSnapshot;

	@Column(nullable = false, columnDefinition = "text")
	private String jobDescriptionSnapshot;

	@Column(nullable = false)
	private Integer matchScore;

	@Column(nullable = false, columnDefinition = "text")
	private String missingKeywordsJson;

	@Column(nullable = false, columnDefinition = "text")
	private String resumeBulletImprovementsJson;

	@Column(nullable = false, columnDefinition = "text")
	private String suggestedSkillsJson;

	@Column(nullable = false, columnDefinition = "text")
	private String coverLetter;

	@Column(nullable = false)
	private String source;

	private String model;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	protected AiAnalysis() {
	}

	public AiAnalysis(
			User user,
			Resume resume,
			JobApplication jobApplication,
			String resumeSnapshot,
			String jobDescriptionSnapshot,
			Integer matchScore,
			String missingKeywordsJson,
			String resumeBulletImprovementsJson,
			String suggestedSkillsJson,
			String coverLetter,
			String source,
			String model
	) {
		this.user = user;
		this.resume = resume;
		this.jobApplication = jobApplication;
		this.resumeSnapshot = resumeSnapshot;
		this.jobDescriptionSnapshot = jobDescriptionSnapshot;
		this.matchScore = matchScore;
		this.missingKeywordsJson = missingKeywordsJson;
		this.resumeBulletImprovementsJson = resumeBulletImprovementsJson;
		this.suggestedSkillsJson = suggestedSkillsJson;
		this.coverLetter = coverLetter;
		this.source = source;
		this.model = model;
	}

	@PrePersist
	void onCreate() {
		this.createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public Resume getResume() {
		return resume;
	}

	public JobApplication getJobApplication() {
		return jobApplication;
	}

	public Integer getMatchScore() {
		return matchScore;
	}

	public String getMissingKeywordsJson() {
		return missingKeywordsJson;
	}

	public String getResumeBulletImprovementsJson() {
		return resumeBulletImprovementsJson;
	}

	public String getSuggestedSkillsJson() {
		return suggestedSkillsJson;
	}

	public String getCoverLetter() {
		return coverLetter;
	}

	public String getSource() {
		return source;
	}

	public String getModel() {
		return model;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
