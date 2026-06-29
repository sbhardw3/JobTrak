package com.jobtrak.backend.dto;

import com.jobtrak.backend.entity.ApplicationStatus;
import jakarta.validation.constraints.NotBlank;

public record JobApplicationRequest(
		@NotBlank(message = "Company is required")
		String company,

		@NotBlank(message = "Job title is required")
		String jobTitle,

		String jobDescription,
		String jobUrl,
		String notes,
		ApplicationStatus status
) {
}
