package com.jobtrak.backend.dto;

public record AuthResponse(
		String message,
		UserResponse user,
		String token
) {
}
