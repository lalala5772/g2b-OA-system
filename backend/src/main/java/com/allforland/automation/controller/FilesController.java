package com.allforland.automation.controller;

import com.allforland.automation.common.ApiResponse;
import com.allforland.automation.common.AuthenticatedPrincipal;
import com.allforland.automation.domain.FilePurpose;
import com.allforland.automation.dto.UploadedFileResponse;
import com.allforland.automation.service.UploadedFileService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Handles one-off request files (contest notices, requirement docs) — see CompanyFileController for the durable company repository. */
@RestController
@RequestMapping("/api/files")
public class FilesController {

	private final UploadedFileService uploadedFileService;

	public FilesController(UploadedFileService uploadedFileService) {
		this.uploadedFileService = uploadedFileService;
	}

	@PostMapping("/upload")
	public ApiResponse<UploadedFileResponse> upload(
			@RequestPart("file") MultipartFile file,
			@RequestParam("purpose") FilePurpose purpose,
			@AuthenticationPrincipal AuthenticatedPrincipal principal) {
		return ApiResponse.ok(uploadedFileService.upload(file, purpose, principal.userId()));
	}
}
