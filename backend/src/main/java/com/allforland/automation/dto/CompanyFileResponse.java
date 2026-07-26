package com.allforland.automation.dto;

import com.allforland.automation.domain.CompanyFile;
import com.allforland.automation.domain.FileCategory;
import com.allforland.automation.domain.ParseStatus;
import java.time.Instant;

public record CompanyFileResponse(
		Long id,
		String fileName,
		String fileType,
		FileCategory category,
		ParseStatus parseStatus,
		Instant uploadedAt,
		String uploadedByName) {

	public static CompanyFileResponse from(CompanyFile file) {
		return new CompanyFileResponse(
				file.getId(),
				file.getFileName(),
				file.getFileType(),
				file.getCategory(),
				file.getParseStatus(),
				file.getUploadedAt(),
				file.getUploadedBy().getName());
	}
}
