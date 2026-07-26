package com.allforland.automation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "google_id", nullable = false, unique = true)
	private String googleId;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	private String name;

	@Column(name = "profile_image_url")
	private String profileImageUrl;

	@Column(nullable = false)
	private String role = "USER";

	@Column(name = "last_login_at", nullable = false)
	private Instant lastLoginAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	public User(String googleId, String email, String name, String profileImageUrl) {
		this.googleId = googleId;
		this.email = email;
		this.name = name;
		this.profileImageUrl = profileImageUrl;
		this.role = "USER";
		this.createdAt = Instant.now();
		this.lastLoginAt = this.createdAt;
	}

	public void touchLogin() {
		this.lastLoginAt = Instant.now();
	}

	public void updateProfile(String name, String profileImageUrl) {
		this.name = name;
		this.profileImageUrl = profileImageUrl;
	}
}
