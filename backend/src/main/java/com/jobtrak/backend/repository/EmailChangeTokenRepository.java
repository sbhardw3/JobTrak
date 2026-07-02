package com.jobtrak.backend.repository;

import com.jobtrak.backend.entity.EmailChangeToken;
import com.jobtrak.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailChangeTokenRepository extends JpaRepository<EmailChangeToken, Long> {

	Optional<EmailChangeToken> findByToken(String token);

	void deleteByUser(User user);
}
