package com.jobtrak.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "job_applications")
public class JobApplication {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	private String company;

	@Column(nullable = false)
	private String jobTitle;

	@Column(columnDefinition = "text")
	private String jobDescription;

	private String jobUrl;

	@Column(columnDefinition = "text")
	private String notes;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ApplicationStatus status;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	protected JobApplication() {
	}

	public JobApplication(
			User user,
			String company,
			String jobTitle,
			String jobDescription,
			String jobUrl,
			String notes,
			ApplicationStatus status
	) {
		this.user = user;
		this.company = company;
		this.jobTitle = jobTitle;
		this.jobDescription = jobDescription;
		this.jobUrl = jobUrl;
		this.notes = notes;
		this.status = status;
	}

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = Instant.now();
	}

	public void update(
			String company,
			String jobTitle,
			String jobDescription,
			String jobUrl,
			String notes,
			ApplicationStatus status
	) {
		this.company = company;
		this.jobTitle = jobTitle;
		this.jobDescription = jobDescription;
		this.jobUrl = jobUrl;
		this.notes = notes;
		this.status = status;
	}

	public void updateStatus(ApplicationStatus status) {
		this.status = status;
	}

	public Long getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public String getCompany() {
		return company;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public String getJobDescription() {
		return jobDescription;
	}

	public String getJobUrl() {
		return jobUrl;
	}

	public String getNotes() {
		return notes;
	}

	public ApplicationStatus getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
