package com.allforland.automation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "zip_exports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ZipExport {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "requirement_set_id", nullable = false)
	private RequirementSet requirementSet;

	@Column(name = "storage_key", nullable = false)
	private String storageKey;

	@Column(name = "matched_count", nullable = false)
	private int matchedCount;

	@Column(name = "missing_count", nullable = false)
	private int missingCount;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	public ZipExport(RequirementSet requirementSet, String storageKey, int matchedCount, int missingCount) {
		this.requirementSet = requirementSet;
		this.storageKey = storageKey;
		this.matchedCount = matchedCount;
		this.missingCount = missingCount;
		this.createdAt = Instant.now();
	}
}
