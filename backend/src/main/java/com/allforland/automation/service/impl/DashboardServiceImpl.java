package com.allforland.automation.service.impl;

import com.allforland.automation.domain.FileCategory;
import com.allforland.automation.dto.DashboardSummaryResponse;
import com.allforland.automation.dto.DashboardSummaryResponse.FeatureStatus;
import com.allforland.automation.repository.CompanyFileRepository;
import com.allforland.automation.service.DashboardService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {

	private final CompanyFileRepository companyFileRepository;

	public DashboardServiceImpl(CompanyFileRepository companyFileRepository) {
		this.companyFileRepository = companyFileRepository;
	}

	@Override
	public DashboardSummaryResponse getSummary() {
		Map<String, Long> byCategory = new LinkedHashMap<>();
		for (FileCategory category : FileCategory.values()) {
			byCategory.put(category.name(), companyFileRepository.countByCategory(category));
		}
		long total = byCategory.values().stream().mapToLong(Long::longValue).sum();

		FeatureStatus comingSoon = new FeatureStatus("not_implemented", "Phase 2+ 제공 예정");
		return new DashboardSummaryResponse(total, byCategory, comingSoon, comingSoon, comingSoon, comingSoon);
	}
}
