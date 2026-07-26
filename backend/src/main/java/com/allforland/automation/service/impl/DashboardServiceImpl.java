package com.allforland.automation.service.impl;

import com.allforland.automation.domain.BidNoticeStatus;
import com.allforland.automation.domain.FileCategory;
import com.allforland.automation.dto.DashboardSummaryResponse;
import com.allforland.automation.dto.DashboardSummaryResponse.FeatureStatus;
import com.allforland.automation.repository.BidNoticeRepository;
import com.allforland.automation.repository.CompanyFileRepository;
import com.allforland.automation.repository.ContestIdeaRepository;
import com.allforland.automation.repository.DocumentTemplateRepository;
import com.allforland.automation.repository.ZipExportRepository;
import com.allforland.automation.service.DashboardService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {

	private final CompanyFileRepository companyFileRepository;
	private final BidNoticeRepository bidNoticeRepository;
	private final DocumentTemplateRepository documentTemplateRepository;
	private final ContestIdeaRepository contestIdeaRepository;
	private final ZipExportRepository zipExportRepository;

	public DashboardServiceImpl(
			CompanyFileRepository companyFileRepository,
			BidNoticeRepository bidNoticeRepository,
			DocumentTemplateRepository documentTemplateRepository,
			ContestIdeaRepository contestIdeaRepository,
			ZipExportRepository zipExportRepository) {
		this.companyFileRepository = companyFileRepository;
		this.bidNoticeRepository = bidNoticeRepository;
		this.documentTemplateRepository = documentTemplateRepository;
		this.contestIdeaRepository = contestIdeaRepository;
		this.zipExportRepository = zipExportRepository;
	}

	@Override
	public DashboardSummaryResponse getSummary() {
		Map<String, Long> byCategory = new LinkedHashMap<>();
		for (FileCategory category : FileCategory.values()) {
			byCategory.put(category.name(), companyFileRepository.countByCategory(category));
		}
		long total = byCategory.values().stream().mapToLong(Long::longValue).sum();

		long notifiedCount = bidNoticeRepository.findTop50ByOrderByCrawledAtDesc().stream()
				.filter(notice -> notice.getStatus() == BidNoticeStatus.NOTIFIED)
				.count();
		FeatureStatus bidStatus = new FeatureStatus("available", notifiedCount + "건 적격 공고 감지");

		long templateCount = documentTemplateRepository.count();
		FeatureStatus documentStatus = new FeatureStatus("available", templateCount + "개 템플릿 등록됨");

		long ideaCount = contestIdeaRepository.count();
		FeatureStatus ideaStatus = new FeatureStatus("available", ideaCount + "개 아이디어 생성됨");

		long zipCount = zipExportRepository.count();
		FeatureStatus evidenceStatus = new FeatureStatus("available", zipCount + "건 증빙 매칭 완료");

		return new DashboardSummaryResponse(total, byCategory, ideaStatus, documentStatus, evidenceStatus, bidStatus);
	}
}
