package com.jobtrak.backend.controller;

import com.jobtrak.backend.dto.AuthResponse;
import com.jobtrak.backend.dto.ForgotPasswordRequest;
import com.jobtrak.backend.dto.LoginRequest;
import com.jobtrak.backend.dto.MessageResponse;
import com.jobtrak.backend.dto.ResetPasswordRequest;
import com.jobtrak.backend.dto.SignupRequest;
import com.jobtrak.backend.dto.TokenRequest;
import com.jobtrak.backend.service.AuthService;
import com.jobtrak.backend.service.UserAccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;
	private final UserAccountService userAccountService;

	public AuthController(AuthService authService, UserAccountService userAccountService) {
		this.authService = authService;
		this.userAccountService = userAccountService;
	}

	@PostMapping("/signup")
	public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@PostMapping("/forgot-password")
	public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
		return authService.requestPasswordReset(request);
	}

	@PostMapping("/reset-password")
	public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
		return authService.resetPassword(request);
	}

	@PostMapping("/verify-email-change")
	public AuthResponse verifyEmailChange(@Valid @RequestBody TokenRequest request) {
		return userAccountService.verifyEmailChange(request);
	}
}
