package com.allforland.automation.dto;

import java.util.Map;

public record DashboardSummaryResponse(
		long totalCompanyFiles,
		Map<String, Long> companyFilesByCategory,
		FeatureStatus document,
		FeatureStatus evidence,
		FeatureStatus bid) {

	public record FeatureStatus(String status, String badge) {
	}
}
