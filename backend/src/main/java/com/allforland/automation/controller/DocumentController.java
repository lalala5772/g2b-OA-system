package com.allforland.automation.controller;

import com.allforland.automation.common.ApiResponse;
import com.allforland.automation.common.NotImplementedResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

	@GetMapping("/templates")
	public ApiResponse<NotImplementedResponse> templates() {
		return ApiResponse.ok(NotImplementedResponse.of("문서 자동 채우기"));
	}

	@PostMapping("/generate")
	public ApiResponse<NotImplementedResponse> generate() {
		return ApiResponse.ok(NotImplementedResponse.of("문서 자동 채우기"));
	}
}
