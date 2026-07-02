package com.jobtrak.backend.service;

import com.jobtrak.backend.dto.AuthResponse;
import com.jobtrak.backend.dto.ForgotPasswordRequest;
import com.jobtrak.backend.dto.LoginRequest;
import com.jobtrak.backend.dto.MessageResponse;
import com.jobtrak.backend.dto.ResetPasswordRequest;
import com.jobtrak.backend.dto.SignupRequest;
import com.jobtrak.backend.dto.UserResponse;
import com.jobtrak.backend.entity.PasswordResetToken;
import com.jobtrak.backend.entity.User;
import com.jobtrak.backend.repository.PasswordResetTokenRepository;
import com.jobtrak.backend.repository.UserRepository;
import com.jobtrak.backend.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AuthService {

	private static final Logger log = LoggerFactory.getLogger(AuthService.class);

	private final UserRepository userRepository;
	private final PasswordResetTokenRepository passwordResetTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final String frontendUrl;

	public AuthService(
			UserRepository userRepository,
			PasswordResetTokenRepository passwordResetTokenRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService,
			@Value("${app.frontend-url}") String frontendUrl
	) {
		this.userRepository = userRepository;
		this.passwordResetTokenRepository = passwordResetTokenRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.frontendUrl = frontendUrl;
	}

	public AuthResponse signup(SignupRequest request) {
		String email = normalizeEmail(request.email());

		if (userRepository.existsByEmail(email)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
		}

		User user = new User(
				request.name().trim(),
				email,
				passwordEncoder.encode(request.password())
		);

		User savedUser = userRepository.save(user);
		return new AuthResponse("Signup successful", toUserResponse(savedUser), jwtService.generateToken(savedUser));
	}

	public AuthResponse login(LoginRequest request) {
		String email = normalizeEmail(request.email());
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
		}

		return new AuthResponse("Login successful", toUserResponse(user), jwtService.generateToken(user));
	}

	@Transactional
	public MessageResponse requestPasswordReset(ForgotPasswordRequest request) {
		String email = normalizeEmail(request.email());

		userRepository.findByEmail(email).ifPresent(user -> {
			passwordResetTokenRepository.deleteByUser(user);
			PasswordResetToken resetToken = new PasswordResetToken(
					user,
					generateToken(),
					Instant.now().plus(30, ChronoUnit.MINUTES)
			);
			passwordResetTokenRepository.save(resetToken);
			log.info("JobTrak password reset link for {}: {}/reset-password?token={}",
					email,
					frontendUrl,
					resetToken.getToken());
		});

		return new MessageResponse("If that email exists, a password reset link has been created.");
	}

	@Transactional
	public MessageResponse resetPassword(ResetPasswordRequest request) {
		PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token().trim())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reset link is invalid or expired"));

		if (resetToken.isUsed() || resetToken.isExpired()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reset link is invalid or expired");
		}

		User user = resetToken.getUser();
		user.updatePasswordHash(passwordEncoder.encode(request.newPassword()));
		resetToken.markUsed();
		userRepository.save(user);
		passwordResetTokenRepository.save(resetToken);

		return new MessageResponse("Password reset successful. You can log in with your new password.");
	}

	private String generateToken() {
		return UUID.randomUUID().toString();
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase();
	}

	private UserResponse toUserResponse(User user) {
		return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
	}
}
