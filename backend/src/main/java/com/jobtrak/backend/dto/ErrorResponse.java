package com.jobtrak.backend.dto;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
		String message,
		int status,
		String path,
		Instant timestamp,
		List<String> errors
) {
	public static ErrorResponse of(String message, int status, String path) {
		return new ErrorResponse(message, status, path, Instant.now(), List.of());
	}

	public static ErrorResponse of(String message, int status, String path, List<String> errors) {
		return new ErrorResponse(message, status, path, Instant.now(), errors);
	}
}
