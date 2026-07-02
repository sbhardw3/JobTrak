package com.jobtrak.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
		@NotBlank(message = "Name is required")
		@Size(max = 120, message = "Name must be 120 characters or fewer")
		String name
) {
}
