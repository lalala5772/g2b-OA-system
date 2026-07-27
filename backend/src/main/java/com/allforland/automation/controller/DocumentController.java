package com.allforland.automation.controller;

import com.allforland.automation.common.ApiResponse;
import com.allforland.automation.common.AuthenticatedPrincipal;
import com.allforland.automation.dto.DocumentGenerationResponse;
import com.allforland.automation.service.DocumentService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

	private final DocumentService documentService;

	public DocumentController(DocumentService documentService) {
		this.documentService = documentService;
	}

	@PostMapping("/auto-fill")
	public ApiResponse<DocumentGenerationResponse> autoFill(
			@RequestPart("file") MultipartFile file, @AuthenticationPrincipal AuthenticatedPrincipal principal) {
		return ApiResponse.ok(documentService.autoFill(file, principal.userId()));
	}

	@GetMapping("/generations/{id}/download")
	public ResponseEntity<byte[]> download(@PathVariable Long id) {
		byte[] content = documentService.downloadGeneration(id);
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.header(
						HttpHeaders.CONTENT_DISPOSITION,
						ContentDisposition.attachment().filename("document-" + id + ".docx").build().toString())
				.body(content);
	}
}
