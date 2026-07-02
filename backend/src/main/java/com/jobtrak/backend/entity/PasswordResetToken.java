package com.jobtrak.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false, unique = true)
	private String token;

	@Column(nullable = false)
	private Instant expiresAt;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	private Instant usedAt;

	protected PasswordResetToken() {
	}

	public PasswordResetToken(User user, String token, Instant expiresAt) {
		this.user = user;
		this.token = token;
		this.expiresAt = expiresAt;
	}

	@PrePersist
	void onCreate() {
		this.createdAt = Instant.now();
	}

	public User getUser() {
		return user;
	}

	public String getToken() {
		return token;
	}

	public boolean isExpired() {
		return Instant.now().isAfter(expiresAt);
	}

	public boolean isUsed() {
		return usedAt != null;
	}

	public void markUsed() {
		this.usedAt = Instant.now();
	}
}
