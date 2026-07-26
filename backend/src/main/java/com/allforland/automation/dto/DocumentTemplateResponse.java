package com.allforland.automation.dto;

import com.allforland.automation.domain.DocumentTemplate;
import java.time.Instant;
import java.util.List;

public record DocumentTemplateResponse(Long id, String name, List<DocumentFieldSchema> fields, Instant createdAt) {

	public static DocumentTemplateResponse of(DocumentTemplate template, List<DocumentFieldSchema> fields) {
		return new DocumentTemplateResponse(template.getId(), template.getName(), fields, template.getCreatedAt());
	}
}
