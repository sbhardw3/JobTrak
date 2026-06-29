package com.jobtrak.backend.controller;

import com.jobtrak.backend.dto.JobApplicationRequest;
import com.jobtrak.backend.dto.JobApplicationResponse;
import com.jobtrak.backend.dto.StatusUpdateRequest;
import com.jobtrak.backend.service.JobApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class JobApplicationController {

	private final JobApplicationService jobApplicationService;

	public JobApplicationController(JobApplicationService jobApplicationService) {
		this.jobApplicationService = jobApplicationService;
	}

	@PostMapping
	public ResponseEntity<JobApplicationResponse> create(
			Authentication authentication,
			@Valid @RequestBody JobApplicationRequest request
	) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(jobApplicationService.create(authentication.getName(), request));
	}

	@GetMapping
	public List<JobApplicationResponse> getAll(Authentication authentication) {
		return jobApplicationService.getAll(authentication.getName());
	}

	@GetMapping("/{id}")
	public JobApplicationResponse getById(Authentication authentication, @PathVariable Long id) {
		return jobApplicationService.getById(authentication.getName(), id);
	}

	@PutMapping("/{id}")
	public JobApplicationResponse update(
			Authentication authentication,
			@PathVariable Long id,
			@Valid @RequestBody JobApplicationRequest request
	) {
		return jobApplicationService.update(authentication.getName(), id, request);
	}

	@PatchMapping("/{id}/status")
	public JobApplicationResponse updateStatus(
			Authentication authentication,
			@PathVariable Long id,
			@Valid @RequestBody StatusUpdateRequest request
	) {
		return jobApplicationService.updateStatus(authentication.getName(), id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
		jobApplicationService.delete(authentication.getName(), id);
		return ResponseEntity.noContent().build();
	}
}
