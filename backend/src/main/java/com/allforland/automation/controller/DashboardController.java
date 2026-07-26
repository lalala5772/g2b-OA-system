package com.allforland.automation.controller;

import com.allforland.automation.common.ApiResponse;
import com.allforland.automation.dto.DashboardSummaryResponse;
import com.allforland.automation.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

	private final DashboardService dashboardService;

	public DashboardController(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}

	@GetMapping("/summary")
	public ApiResponse<DashboardSummaryResponse> summary() {
		return ApiResponse.ok(dashboardService.getSummary());
	}
}
