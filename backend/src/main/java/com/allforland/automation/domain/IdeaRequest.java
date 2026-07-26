package com.allforland.automation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "idea_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdeaRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contest_file_id", nullable = false)
	private UploadedFile contestFile;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private IdeaRequestStatus status = IdeaRequestStatus.PROCESSING;

	@Column(name = "requested_at", nullable = false)
	private Instant requestedAt;

	public IdeaRequest(User user, UploadedFile contestFile) {
		this.user = user;
		this.contestFile = contestFile;
		this.status = IdeaRequestStatus.PROCESSING;
		this.requestedAt = Instant.now();
	}

	public void markCompleted() {
		this.status = IdeaRequestStatus.COMPLETED;
	}

	public void markFailed() {
		this.status = IdeaRequestStatus.FAILED;
	}
}
