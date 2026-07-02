package com.jobtrak.backend.controller;

import com.jobtrak.backend.dto.MessageResponse;
import com.jobtrak.backend.dto.RequestEmailChangeRequest;
import com.jobtrak.backend.dto.UpdateProfileRequest;
import com.jobtrak.backend.dto.UserResponse;
import com.jobtrak.backend.service.UserAccountService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserAccountService userAccountService;

	public UserController(UserAccountService userAccountService) {
		this.userAccountService = userAccountService;
	}

	@GetMapping("/me")
	public UserResponse currentUser(Authentication authentication) {
		return userAccountService.currentUser(authentication.getName());
	}

	@PatchMapping("/me")
	public UserResponse updateProfile(
			Authentication authentication,
			@Valid @RequestBody UpdateProfileRequest request
	) {
		return userAccountService.updateProfile(authentication.getName(), request);
	}

	@PostMapping("/me/email-change")
	public MessageResponse requestEmailChange(
			Authentication authentication,
			@Valid @RequestBody RequestEmailChangeRequest request
	) {
		return userAccountService.requestEmailChange(authentication.getName(), request);
	}
}
