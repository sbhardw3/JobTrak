package com.jobtrak.backend.controller;

import com.jobtrak.backend.dto.AiAnalysisRequest;
import com.jobtrak.backend.dto.AiAnalysisResponse;
import com.jobtrak.backend.service.AiAnalysisService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiAnalysisController {

	private final AiAnalysisService aiAnalysisService;

	public AiAnalysisController(AiAnalysisService aiAnalysisService) {
		this.aiAnalysisService = aiAnalysisService;
	}

	@PostMapping("/analyze")
	public ResponseEntity<AiAnalysisResponse> analyze(
			Authentication authentication,
			@RequestBody AiAnalysisRequest request
	) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(aiAnalysisService.analyze(authentication.getName(), request));
	}

	@GetMapping("/analyses")
	public List<AiAnalysisResponse> getAll(Authentication authentication) {
		return aiAnalysisService.getAll(authentication.getName());
	}

	@GetMapping("/analyses/{id}")
	public AiAnalysisResponse getById(Authentication authentication, @PathVariable Long id) {
		return aiAnalysisService.getById(authentication.getName(), id);
	}
}
