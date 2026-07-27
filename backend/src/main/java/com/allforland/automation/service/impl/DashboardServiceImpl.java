package com.allforland.automation.service.impl;

import com.allforland.automation.domain.BidNoticeStatus;
import com.allforland.automation.domain.FileCategory;
import com.allforland.automation.dto.DashboardSummaryResponse;
import com.allforland.automation.dto.DashboardSummaryResponse.FeatureStatus;
import com.allforland.automation.repository.BidNoticeRepository;
import com.allforland.automation.repository.CompanyFileRepository;
import com.allforland.automation.repository.DocumentGenerationRepository;
import com.allforland.automation.repository.ZipExportRepository;
import com.allforland.automation.service.DashboardService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {

	private final CompanyFileRepository companyFileRepository;
	private final BidNoticeRepository bidNoticeRepository;
	private final DocumentGenerationRepository documentGenerationRepository;
	private final ZipExportRepository zipExportRepository;

	public DashboardServiceImpl(
			CompanyFileRepository companyFileRepository,
			BidNoticeRepository bidNoticeRepository,
			DocumentGenerationRepository documentGenerationRepository,
			ZipExportRepository zipExportRepository) {
		this.companyFileRepository = companyFileRepository;
		this.bidNoticeRepository = bidNoticeRepository;
		this.documentGenerationRepository = documentGenerationRepository;
		this.zipExportRepository = zipExportRepository;
	}

	@Override
	public DashboardSummaryResponse getSummary() {
		Map<String, Long> byCategory = new LinkedHashMap<>();
		for (FileCategory category : FileCategory.values()) {
			byCategory.put(category.name(), companyFileRepository.countByCategory(category));
		}
		long total = byCategory.values().stream().mapToLong(Long::longValue).sum();

		long eligibleCount = bidNoticeRepository.countByStatus(BidNoticeStatus.ELIGIBLE);
		FeatureStatus bidStatus = new FeatureStatus("available", eligibleCount + "건 적격 공고 감지");

		long generationCount = documentGenerationRepository.count();
		FeatureStatus documentStatus = new FeatureStatus("available", generationCount + "건 문서 자동 채움 완료");

		long zipCount = zipExportRepository.count();
		FeatureStatus evidenceStatus = new FeatureStatus("available", zipCount + "건 증빙 매칭 완료");

		return new DashboardSummaryResponse(total, byCategory, documentStatus, evidenceStatus, bidStatus);
	}
}
