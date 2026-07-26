package com.allforland.automation.controller;

import com.allforland.automation.common.ApiResponse;
import com.allforland.automation.common.AuthenticatedPrincipal;
import com.allforland.automation.dto.ContestIdeaResponse;
import com.allforland.automation.dto.IdeaGenerateRequest;
import com.allforland.automation.dto.IdeaGenerateResponse;
import com.allforland.automation.service.IdeaService;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ideas")
public class IdeaController {

	private final IdeaService ideaService;

	public IdeaController(IdeaService ideaService) {
		this.ideaService = ideaService;
	}

	@GetMapping
	public ApiResponse<List<ContestIdeaResponse>> recent() {
		return ApiResponse.ok(ideaService.recentIdeas());
	}

	@PostMapping("/generate")
	public ApiResponse<IdeaGenerateResponse> generate(
			@RequestBody IdeaGenerateRequest request, @AuthenticationPrincipal AuthenticatedPrincipal principal) {
		return ApiResponse.ok(ideaService.generate(request, principal.userId()));
	}
}
