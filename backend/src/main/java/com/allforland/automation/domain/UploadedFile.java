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

/** One-off files for a single request (contest notice, requirement doc) — see CompanyFile for durable company assets. */
@Entity
@Table(name = "uploaded_files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UploadedFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "original_name", nullable = false)
	private String originalName;

	@Column(name = "file_type", nullable = false)
	private String fileType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private FilePurpose purpose;

	@Column(name = "storage_key", nullable = false)
	private String storageKey;

	@Column(name = "extracted_text", columnDefinition = "TEXT")
	private String extractedText;

	@Enumerated(EnumType.STRING)
	@Column(name = "parse_status", nullable = false)
	private ParseStatus parseStatus = ParseStatus.PENDING;

	@Column(name = "uploaded_at", nullable = false)
	private Instant uploadedAt;

	public UploadedFile(User user, String originalName, String fileType, FilePurpose purpose, String storageKey) {
		this.user = user;
		this.originalName = originalName;
		this.fileType = fileType;
		this.purpose = purpose;
		this.storageKey = storageKey;
		this.parseStatus = ParseStatus.PENDING;
		this.uploadedAt = Instant.now();
	}

	public void markParsed(String extractedText) {
		this.extractedText = extractedText;
		this.parseStatus = ParseStatus.SUCCESS;
	}

	public void markParseFailed() {
		this.parseStatus = ParseStatus.FAILED;
	}
}
