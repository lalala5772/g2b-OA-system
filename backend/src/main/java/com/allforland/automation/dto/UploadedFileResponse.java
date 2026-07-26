package com.allforland.automation.dto;

import com.allforland.automation.domain.FilePurpose;
import com.allforland.automation.domain.ParseStatus;
import com.allforland.automation.domain.UploadedFile;
import java.time.Instant;

public record UploadedFileResponse(
		Long id,
		String originalName,
		String fileType,
		FilePurpose purpose,
		ParseStatus parseStatus,
		String extractedText,
		Instant uploadedAt) {

	public static UploadedFileResponse from(UploadedFile file) {
		return new UploadedFileResponse(
				file.getId(),
				file.getOriginalName(),
				file.getFileType(),
				file.getPurpose(),
				file.getParseStatus(),
				file.getExtractedText(),
				file.getUploadedAt());
	}
}
