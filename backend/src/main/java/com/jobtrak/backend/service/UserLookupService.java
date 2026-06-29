package com.jobtrak.backend.service;

import com.jobtrak.backend.entity.User;
import com.jobtrak.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserLookupService {

	private final UserRepository userRepository;

	public UserLookupService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public User getByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
	}
}
