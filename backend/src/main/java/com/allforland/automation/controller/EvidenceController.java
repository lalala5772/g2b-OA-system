package com.allforland.automation.controller;

import com.allforland.automation.common.ApiResponse;
import com.allforland.automation.common.AuthenticatedPrincipal;
import com.allforland.automation.dto.EvidenceAnalysisResponse;
import com.allforland.automation.service.EvidenceService;
import java.util.Map;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/evidence")
public class EvidenceController {

	private final EvidenceService evidenceService;

	public EvidenceController(EvidenceService evidenceService) {
		this.evidenceService = evidenceService;
	}

	@PostMapping("/analyze")
	public ApiResponse<EvidenceAnalysisResponse> analyze(
			@RequestBody Map<String, Long> body, @AuthenticationPrincipal AuthenticatedPrincipal principal) {
		Long uploadedFileId = body.get("uploadedFileId");
		if (uploadedFileId == null) {
			throw new IllegalArgumentException("uploadedFileId가 필요합니다.");
		}
		return ApiResponse.ok(evidenceService.analyze(uploadedFileId, principal.userId()));
	}

	@GetMapping("/exports/{zipExportId}/download")
	public ResponseEntity<byte[]> download(@PathVariable Long zipExportId) {
		byte[] content = evidenceService.downloadZip(zipExportId);
		return ResponseEntity.ok()
				.contentType(MediaType.valueOf("application/zip"))
				.header(
						HttpHeaders.CONTENT_DISPOSITION,
						ContentDisposition.attachment().filename("evidence-" + zipExportId + ".zip").build().toString())
				.body(content);
	}
}
