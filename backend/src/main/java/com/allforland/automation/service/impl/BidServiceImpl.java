package com.allforland.automation.service.impl;

import com.allforland.automation.client.AiEngineClient;
import com.allforland.automation.client.BidScanResult;
import com.allforland.automation.common.BidScanWindow;
import com.allforland.automation.domain.BidKeyword;
import com.allforland.automation.domain.BidNotice;
import com.allforland.automation.domain.BidNoticeStatus;
import com.allforland.automation.domain.FileCategory;
import com.allforland.automation.dto.BidKeywordResponse;
import com.allforland.automation.dto.BidNoticeResponse;
import com.allforland.automation.dto.BidScanSummaryResponse;
import com.allforland.automation.dto.BidWindowResponse;
import com.allforland.automation.repository.BidKeywordRepository;
import com.allforland.automation.repository.BidNoticeRepository;
import com.allforland.automation.repository.CompanyFileRepository;
import com.allforland.automation.service.BidService;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BidServiceImpl implements BidService {

	private static final int COMPANY_PROFILE_MAX_CHARS = 4000;

	private final BidKeywordRepository bidKeywordRepository;
	private final BidNoticeRepository bidNoticeRepository;
	private final CompanyFileRepository companyFileRepository;
	private final AiEngineClient aiEngineClient;
	private final double eligibilityThreshold;

	public BidServiceImpl(
			BidKeywordRepository bidKeywordRepository,
			BidNoticeRepository bidNoticeRepository,
			CompanyFileRepository companyFileRepository,
			AiEngineClient aiEngineClient,
			@Value("${app.bid.eligibility-threshold}") double eligibilityThreshold) {
		this.bidKeywordRepository = bidKeywordRepository;
		this.bidNoticeRepository = bidNoticeRepository;
		this.companyFileRepository = companyFileRepository;
		this.aiEngineClient = aiEngineClient;
		this.eligibilityThreshold = eligibilityThreshold;
	}

	@Override
	@Transactional
	public BidScanSummaryResponse triggerScan() {
		List<String> keywords = bidKeywordRepository.findAllByActiveTrue().stream()
				.map(BidKeyword::getKeyword)
				.toList();
		if (keywords.isEmpty()) {
			return new BidScanSummaryResponse(0, 0, 0);
		}

		String companyProfile = buildCompanyProfile();
		List<BidScanResult> results = aiEngineClient.scanBids(keywords, companyProfile, eligibilityThreshold);

		int newCount = 0;
		int eligibleCount = 0;

		for (BidScanResult result : results) {
			if (result.externalBidNo() == null || bidNoticeRepository.findByExternalBidNo(result.externalBidNo()).isPresent()) {
				continue;
			}
			newCount++;

			BidNotice notice = new BidNotice(
					result.externalBidNo(),
					result.title(),
					result.agency(),
					result.matchedKeyword(),
					parseDate(result.announceDate()),
					parseDate(result.deadline()),
					result.url());
			notice.applyEligibility(result.eligibilityScore(), result.aiJudgement(), result.eligible());
			if (result.eligible()) {
				eligibleCount++;
			}

			bidNoticeRepository.save(notice);
		}

		return new BidScanSummaryResponse(results.size(), newCount, eligibleCount);
	}

	@Override
	@Transactional(readOnly = true)
	public BidWindowResponse eligibleInCurrentWindow() {
		BidScanWindow.Window window = BidScanWindow.current();
		List<BidNoticeResponse> notices = bidNoticeRepository
				.findAllByStatusAndCrawledAtBetweenOrderByEligibilityScoreDesc(
						BidNoticeStatus.ELIGIBLE, window.start(), window.end())
				.stream()
				.map(BidNoticeResponse::from)
				.toList();
		return new BidWindowResponse(window.start(), window.end(), notices);
	}

	@Override
	@Transactional(readOnly = true)
	public List<BidKeywordResponse> listKeywords() {
		return bidKeywordRepository.findAll().stream().map(BidKeywordResponse::from).toList();
	}

	@Override
	@Transactional
	public BidKeywordResponse addKeyword(String keyword) {
		return BidKeywordResponse.from(bidKeywordRepository.save(new BidKeyword(keyword)));
	}

	@Override
	@Transactional
	public void removeKeyword(Long id) {
		bidKeywordRepository.deleteById(id);
	}

	private String buildCompanyProfile() {
		String profile = companyFileRepository
				.findAllByCategoryOrderByUploadedAtDesc(FileCategory.DOMAIN_INTRO)
				.stream()
				.limit(3)
				.map(file -> file.getExtractedText() == null ? "" : file.getExtractedText())
				.collect(Collectors.joining("\n\n"));
		return profile.length() > COMPANY_PROFILE_MAX_CHARS ? profile.substring(0, COMPANY_PROFILE_MAX_CHARS) : profile;
	}

	private LocalDate parseDate(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return LocalDate.parse(value);
		} catch (DateTimeParseException e) {
			return null;
		}
	}
}
