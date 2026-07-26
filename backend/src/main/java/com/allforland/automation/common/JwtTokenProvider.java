package com.allforland.automation.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

	private final Key signingKey;
	private final long expirationMinutes;

	public JwtTokenProvider(
			@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
		this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expirationMinutes = expirationMinutes;
	}

	public String generateToken(Long userId, String email, String role) {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject(String.valueOf(userId))
				.claim("email", email)
				.claim("role", role)
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plus(Duration.ofMinutes(expirationMinutes))))
				.signWith(signingKey)
				.compact();
	}

	public long expirationSeconds() {
		return Duration.ofMinutes(expirationMinutes).toSeconds();
	}

	public Optional<AuthenticatedPrincipal> parse(String token) {
		try {
			Claims claims = Jwts.parser()
					.verifyWith((javax.crypto.SecretKey) signingKey)
					.build()
					.parseSignedClaims(token)
					.getPayload();
			Long userId = Long.valueOf(claims.getSubject());
			String email = claims.get("email", String.class);
			String role = claims.get("role", String.class);
			return Optional.of(new AuthenticatedPrincipal(userId, email, role));
		} catch (JwtException | IllegalArgumentException ex) {
			return Optional.empty();
		}
	}
}
