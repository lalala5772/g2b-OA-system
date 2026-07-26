package com.allforland.automation.controller;

import com.allforland.automation.common.ApiResponse;
import com.allforland.automation.common.NotImplementedResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/evidence")
public class EvidenceController {

	@PostMapping("/analyze")
	public ApiResponse<NotImplementedResponse> analyze() {
		return ApiResponse.ok(NotImplementedResponse.of("적격증빙자료 매칭"));
	}
}
