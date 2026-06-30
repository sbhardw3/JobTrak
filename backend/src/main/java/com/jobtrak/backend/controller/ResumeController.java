package com.jobtrak.backend.controller;

import com.jobtrak.backend.dto.ResumeRequest;
import com.jobtrak.backend.dto.ResumeResponse;
import com.jobtrak.backend.dto.ResumeUploadResponse;
import com.jobtrak.backend.service.ResumeFileParserService;
import com.jobtrak.backend.service.ResumeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

	private final ResumeService resumeService;
	private final ResumeFileParserService resumeFileParserService;

	public ResumeController(ResumeService resumeService, ResumeFileParserService resumeFileParserService) {
		this.resumeService = resumeService;
		this.resumeFileParserService = resumeFileParserService;
	}

	@PostMapping
	public ResponseEntity<ResumeResponse> create(
			Authentication authentication,
			@Valid @RequestBody ResumeRequest request
	) {
		return ResponseEntity.status(HttpStatus.CREATED).body(resumeService.create(authentication.getName(), request));
	}

	@PostMapping("/extract")
	public ResumeUploadResponse extract(@RequestParam("file") MultipartFile file) {
		return resumeFileParserService.parse(file);
	}

	@GetMapping
	public List<ResumeResponse> getAll(Authentication authentication) {
		return resumeService.getAll(authentication.getName());
	}

	@GetMapping("/{id}")
	public ResumeResponse getById(Authentication authentication, @PathVariable Long id) {
		return resumeService.getById(authentication.getName(), id);
	}

	@PutMapping("/{id}")
	public ResumeResponse update(
			Authentication authentication,
			@PathVariable Long id,
			@Valid @RequestBody ResumeRequest request
	) {
		return resumeService.update(authentication.getName(), id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
		resumeService.delete(authentication.getName(), id);
		return ResponseEntity.noContent().build();
	}
}
