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
@Table(name = "company_files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "uploaded_by", nullable = false)
	private User uploadedBy;

	@Column(name = "file_name", nullable = false)
	private String fileName;

	@Column(name = "file_type", nullable = false)
	private String fileType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private FileCategory category;

	@Column(name = "storage_key", nullable = false)
	private String storageKey;

	@Column(name = "extracted_text", columnDefinition = "TEXT")
	private String extractedText;

	@Enumerated(EnumType.STRING)
	@Column(name = "parse_status", nullable = false)
	private ParseStatus parseStatus = ParseStatus.PENDING;

	@Column(name = "uploaded_at", nullable = false)
	private Instant uploadedAt;

	public CompanyFile(User uploadedBy, String fileName, String fileType, FileCategory category, String storageKey) {
		this.uploadedBy = uploadedBy;
		this.fileName = fileName;
		this.fileType = fileType;
		this.category = category;
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
