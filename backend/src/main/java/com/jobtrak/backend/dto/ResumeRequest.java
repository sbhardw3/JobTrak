package com.jobtrak.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record ResumeRequest(
		@NotBlank(message = "Resume title is required")
		String title,

		@NotBlank(message = "Resume content is required")
		String content
) {
}
