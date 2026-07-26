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
@Table(name = "requirement_sets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RequirementSet {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "uploaded_file_id", nullable = false)
	private UploadedFile uploadedFile;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private RequirementSetStatus status = RequirementSetStatus.PROCESSING;

	@Column(name = "analyzed_at", nullable = false)
	private Instant analyzedAt;

	public RequirementSet(User user, UploadedFile uploadedFile) {
		this.user = user;
		this.uploadedFile = uploadedFile;
		this.status = RequirementSetStatus.PROCESSING;
		this.analyzedAt = Instant.now();
	}

	public void markCompleted() {
		this.status = RequirementSetStatus.COMPLETED;
	}

	public void markFailed() {
		this.status = RequirementSetStatus.FAILED;
	}
}
