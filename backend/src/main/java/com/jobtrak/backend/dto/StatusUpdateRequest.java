package com.jobtrak.backend.dto;

import com.jobtrak.backend.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
		@NotNull(message = "Status is required")
		ApplicationStatus status
) {
}
