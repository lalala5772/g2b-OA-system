package com.allforland.automation.controller;

import com.allforland.automation.common.ApiResponse;
import com.allforland.automation.common.AuthenticatedPrincipal;
import com.allforland.automation.domain.FileCategory;
import com.allforland.automation.dto.CompanyFileResponse;
import com.allforland.automation.service.CompanyFileService;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/company-files")
public class CompanyFileController {

	private final CompanyFileService companyFileService;

	public CompanyFileController(CompanyFileService companyFileService) {
		this.companyFileService = companyFileService;
	}

	@PostMapping("/upload")
	public ApiResponse<CompanyFileResponse> upload(
			@RequestPart("file") MultipartFile file,
			@RequestParam("category") FileCategory category,
			@AuthenticationPrincipal AuthenticatedPrincipal principal) {
		return ApiResponse.ok(companyFileService.upload(file, category, principal.userId()));
	}

	@GetMapping
	public ApiResponse<List<CompanyFileResponse>> list(
			@RequestParam(value = "category", required = false) FileCategory category) {
		return ApiResponse.ok(companyFileService.list(category));
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(@PathVariable Long id) {
		companyFileService.delete(id);
		return ApiResponse.ok(null);
	}
}
