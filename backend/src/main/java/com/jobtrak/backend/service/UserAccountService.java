package com.jobtrak.backend.service;

import com.jobtrak.backend.dto.AuthResponse;
import com.jobtrak.backend.dto.MessageResponse;
import com.jobtrak.backend.dto.RequestEmailChangeRequest;
import com.jobtrak.backend.dto.TokenRequest;
import com.jobtrak.backend.dto.UpdateProfileRequest;
import com.jobtrak.backend.dto.UserResponse;
import com.jobtrak.backend.entity.EmailChangeToken;
import com.jobtrak.backend.entity.User;
import com.jobtrak.backend.repository.EmailChangeTokenRepository;
import com.jobtrak.backend.repository.UserRepository;
import com.jobtrak.backend.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class UserAccountService {

	private static final Logger log = LoggerFactory.getLogger(UserAccountService.class);

	private final UserRepository userRepository;
	private final EmailChangeTokenRepository emailChangeTokenRepository;
	private final UserLookupService userLookupService;
	private final JwtService jwtService;
	private final String frontendUrl;

	public UserAccountService(
			UserRepository userRepository,
			EmailChangeTokenRepository emailChangeTokenRepository,
			UserLookupService userLookupService,
			JwtService jwtService,
			@Value("${app.frontend-url}") String frontendUrl
	) {
		this.userRepository = userRepository;
		this.emailChangeTokenRepository = emailChangeTokenRepository;
		this.userLookupService = userLookupService;
		this.jwtService = jwtService;
		this.frontendUrl = frontendUrl;
	}

	public UserResponse currentUser(String email) {
		return toUserResponse(userLookupService.getByEmail(email));
	}

	public UserResponse updateProfile(String email, UpdateProfileRequest request) {
		User user = userLookupService.getByEmail(email);
		user.updateName(request.name().trim());
		return toUserResponse(userRepository.save(user));
	}

	@Transactional
	public MessageResponse requestEmailChange(String email, RequestEmailChangeRequest request) {
		User user = userLookupService.getByEmail(email);
		String newEmail = normalizeEmail(request.newEmail());

		if (newEmail.equals(user.getEmail())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New email must be different from your current email");
		}

		if (userRepository.existsByEmail(newEmail)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
		}

		emailChangeTokenRepository.deleteByUser(user);
		EmailChangeToken emailChangeToken = new EmailChangeToken(
				user,
				generateToken(),
				newEmail,
				Instant.now().plus(30, ChronoUnit.MINUTES)
		);
		emailChangeTokenRepository.save(emailChangeToken);

		log.info("JobTrak email verification link for {} changing to {}: {}/verify-email?token={}",
				user.getEmail(),
				newEmail,
				frontendUrl,
				emailChangeToken.getToken());

		return new MessageResponse("Verification link created. Check the Spring Boot terminal in development.");
	}

	@Transactional
	public AuthResponse verifyEmailChange(TokenRequest request) {
		EmailChangeToken emailChangeToken = emailChangeTokenRepository.findByToken(request.token().trim())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email verification link is invalid or expired"));

		if (emailChangeToken.isUsed() || emailChangeToken.isExpired()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email verification link is invalid or expired");
		}

		String newEmail = emailChangeToken.getNewEmail();
		if (userRepository.existsByEmail(newEmail)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
		}

		User user = emailChangeToken.getUser();
		user.updateEmail(newEmail);
		emailChangeToken.markUsed();
		User savedUser = userRepository.save(user);
		emailChangeTokenRepository.save(emailChangeToken);

		return new AuthResponse("Email verified successfully", toUserResponse(savedUser), jwtService.generateToken(savedUser));
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase();
	}

	private String generateToken() {
		return UUID.randomUUID().toString();
	}

	private UserResponse toUserResponse(User user) {
		return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
	}
}
