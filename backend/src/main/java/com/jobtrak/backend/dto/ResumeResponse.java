package com.jobtrak.backend.dto;

import java.time.Instant;

public record ResumeResponse(
		Long id,
		String title,
		String content,
		Instant createdAt,
		Instant updatedAt
) {
}
