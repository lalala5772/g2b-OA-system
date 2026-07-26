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

/** fieldsSchemaJson holds a JSON array of {"key","label","auto"} — see DocumentFieldSchema. */
@Entity
@Table(name = "document_templates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentTemplate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(name = "storage_key", nullable = false)
	private String storageKey;

	@Column(name = "fields_schema_json", nullable = false, columnDefinition = "TEXT")
	private String fieldsSchemaJson;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	public DocumentTemplate(String name, String storageKey, String fieldsSchemaJson) {
		this.name = name;
		this.storageKey = storageKey;
		this.fieldsSchemaJson = fieldsSchemaJson;
		this.createdAt = Instant.now();
	}
}
