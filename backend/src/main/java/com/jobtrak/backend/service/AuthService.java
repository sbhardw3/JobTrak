package com.jobtrak.backend.service;

import com.jobtrak.backend.dto.AuthResponse;
import com.jobtrak.backend.dto.LoginRequest;
import com.jobtrak.backend.dto.SignupRequest;
import com.jobtrak.backend.dto.UserResponse;
import com.jobtrak.backend.entity.User;
import com.jobtrak.backend.repository.UserRepository;
import com.jobtrak.backend.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
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

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase();
	}

	private UserResponse toUserResponse(User user) {
		return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
	}
}
