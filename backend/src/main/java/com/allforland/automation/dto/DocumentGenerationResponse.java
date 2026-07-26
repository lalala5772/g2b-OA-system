package com.allforland.automation.dto;

import com.allforland.automation.domain.DocumentGeneration;
import com.allforland.automation.domain.DocumentGenerationStatus;
import java.time.Instant;
import java.util.Map;

public record DocumentGenerationResponse(
		Long id, DocumentGenerationStatus status, Map<String, String> autoFilledFields, Instant createdAt) {

	public static DocumentGenerationResponse of(DocumentGeneration generation, Map<String, String> autoFilledFields) {
		return new DocumentGenerationResponse(
				generation.getId(), generation.getStatus(), autoFilledFields, generation.getCreatedAt());
	}
}
