package com.allforland.automation.controller;

import com.allforland.automation.common.ApiResponse;
import com.allforland.automation.common.NotImplementedResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ideas")
public class IdeaController {

	@GetMapping
	public ApiResponse<NotImplementedResponse> list() {
		return ApiResponse.ok(NotImplementedResponse.of("아이디어 제안"));
	}

	@PostMapping("/generate")
	public ApiResponse<NotImplementedResponse> generate() {
		return ApiResponse.ok(NotImplementedResponse.of("아이디어 제안"));
	}
}
