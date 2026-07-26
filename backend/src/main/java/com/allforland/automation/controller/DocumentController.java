package com.allforland.automation.controller;

import com.allforland.automation.common.ApiResponse;
import com.allforland.automation.common.AuthenticatedPrincipal;
import com.allforland.automation.dto.DocumentGenerateRequest;
import com.allforland.automation.dto.DocumentGenerationResponse;
import com.allforland.automation.dto.DocumentTemplateResponse;
import com.allforland.automation.service.DocumentService;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;
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

	@GetMapping("/templates")
	public ApiResponse<List<DocumentTemplateResponse>> templates() {
		return ApiResponse.ok(documentService.listTemplates());
	}

	@PostMapping("/templates")
	public ApiResponse<DocumentTemplateResponse> uploadTemplate(
			@RequestPart("file") MultipartFile file,
			@RequestParam("name") String name,
			@RequestParam("fieldsSchema") String fieldsSchemaJson) {
		return ApiResponse.ok(documentService.uploadTemplate(file, name, fieldsSchemaJson));
	}

	@PostMapping("/generate")
	public ApiResponse<DocumentGenerationResponse> generate(
			@RequestBody DocumentGenerateRequest request, @AuthenticationPrincipal AuthenticatedPrincipal principal) {
		return ApiResponse.ok(documentService.generate(request, principal.userId()));
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
