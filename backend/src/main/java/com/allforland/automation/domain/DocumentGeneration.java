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
@Table(name = "document_generations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentGeneration {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "original_filename", nullable = false)
	private String originalFilename;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "filled_fields_json", nullable = false, columnDefinition = "TEXT")
	private String filledFieldsJson;

	@Column(name = "output_storage_key")
	private String outputStorageKey;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DocumentGenerationStatus status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	public DocumentGeneration(String originalFilename, User user, String filledFieldsJson) {
		this.originalFilename = originalFilename;
		this.user = user;
		this.filledFieldsJson = filledFieldsJson;
		this.status = DocumentGenerationStatus.FAILED;
		this.createdAt = Instant.now();
	}

	public void markSuccess(String outputStorageKey) {
		this.outputStorageKey = outputStorageKey;
		this.status = DocumentGenerationStatus.SUCCESS;
	}
}
