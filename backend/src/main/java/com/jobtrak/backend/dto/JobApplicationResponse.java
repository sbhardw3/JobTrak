package com.jobtrak.backend.dto;

import com.jobtrak.backend.entity.ApplicationStatus;

import java.time.Instant;

public record JobApplicationResponse(
		Long id,
		String company,
		String jobTitle,
		String jobDescription,
		String jobUrl,
		String notes,
		ApplicationStatus status,
		Instant createdAt,
		Instant updatedAt
) {
}
