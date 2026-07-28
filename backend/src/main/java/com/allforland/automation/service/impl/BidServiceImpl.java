package com.allforland.automation.service.impl;

import com.allforland.automation.client.AiEngineClient;
import com.allforland.automation.client.BidScanOutcome;
import com.allforland.automation.client.BidScanResult;
import com.allforland.automation.domain.BidKeyword;
import com.allforland.automation.domain.BidNotice;
import com.allforland.automation.domain.BidNoticeStatus;
import com.allforland.automation.domain.FileCategory;
import com.allforland.automation.dto.BidKeywordResponse;
import com.allforland.automation.dto.BidNoticeResponse;
import com.allforland.automation.dto.BidScanSummaryResponse;
import com.allforland.automation.repository.BidKeywordRepository;
import com.allforland.automation.repository.BidNoticeRepository;
import com.allforland.automation.repository.CompanyFileRepository;
import com.allforland.automation.service.BidService;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BidServiceImpl implements BidService {

	private static final int COMPANY_PROFILE_MAX_CHARS = 8000;
	private static final int MAX_KEYWORDS = 15;

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
	public BidScanSummaryResponse triggerScan(LocalDate startDate, LocalDate endDate) {
		validateRange(startDate, endDate);
		List<String> keywords = bidKeywordRepository.findAllByActiveTrue().stream()
				.map(BidKeyword::getKeyword)
				.toList();
		if (keywords.isEmpty()) {
			return new BidScanSummaryResponse(0, 0, 0, 0, 0, 0, startDate, endDate);
		}

		String companyProfile = buildCompanyProfile();
		BidScanOutcome outcome =
				aiEngineClient.scanBids(keywords, companyProfile, eligibilityThreshold, startDate, endDate);

		int newCount = 0;
		int eligibleCount = 0;

		for (BidScanResult result : outcome.results()) {
			if (result.externalBidNo() == null
					|| bidNoticeRepository.findByExternalBidNo(result.externalBidNo()).isPresent()) {
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
			notice.applyEligibility(result.eligibilityScore(), result.aiSummary(), result.aiJudgement(), result.eligible());
			if (result.eligible()) {
				eligibleCount++;
			}

			bidNoticeRepository.save(notice);
		}

		return new BidScanSummaryResponse(
				outcome.fetched(),
				outcome.results().size(),
				newCount,
				eligibleCount,
				outcome.judged(),
				outcome.unjudged(),
				outcome.rangeStart(),
				outcome.rangeEnd());
	}

	@Override
	@Transactional(readOnly = true)
	public List<BidNoticeResponse> recentEligible(LocalDate startDate, LocalDate endDate) {
		validateRange(startDate, endDate);

		// 키워드를 삭제하면 그 키워드로 찾았던 예전 적격 공고도 더 이상 "지금의 관심사"가 아니므로
		// 함께 사라져야 한다 — matchedKeyword가 현재 활성 키워드 목록에 없으면 후보에서 제외.
		List<String> activeKeywords =
				bidKeywordRepository.findAllByActiveTrue().stream().map(BidKeyword::getKeyword).toList();
		if (activeKeywords.isEmpty()) {
			return List.of();
		}

		LocalDate[] range = resolveRange(startDate, endDate);
		return bidNoticeRepository
				.findTop50ByStatusAndAnnounceDateBetweenAndMatchedKeywordInOrderByCrawledAtDesc(
						BidNoticeStatus.ELIGIBLE, range[0], range[1], activeKeywords)
				.stream()
				.map(BidNoticeResponse::from)
				.toList();
	}

	/** startDate/endDate가 둘 다 비어있으면 안 되고, 둘 중 하나만 비어있어도 안 됨 — 시작일은 종료일보다 늦을 수 없음. */
	private void validateRange(LocalDate startDate, LocalDate endDate) {
		if ((startDate == null) != (endDate == null)) {
			throw new IllegalArgumentException("조회 시작일과 종료일을 모두 입력하거나, 모두 비워주세요.");
		}
		if (startDate != null && startDate.isAfter(endDate)) {
			throw new IllegalArgumentException("조회 시작일은 종료일보다 늦을 수 없습니다.");
		}
	}

	/** ai-engine의 기본 조회기간(최근 7일)과 동일한 기본값을 사용해, 스캔과 적격목록 조회가 같은 범위를 바라보게 함. */
	private LocalDate[] resolveRange(LocalDate startDate, LocalDate endDate) {
		if (startDate == null) {
			LocalDate today = LocalDate.now();
			return new LocalDate[] {today.minusDays(7), today};
		}
		return new LocalDate[] {startDate, endDate};
	}

	@Override
	@Transactional(readOnly = true)
	public BidNoticeResponse getById(Long id) {
		return bidNoticeRepository
				.findById(id)
				.map(BidNoticeResponse::from)
				.orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다: " + id));
	}

	@Override
	@Transactional(readOnly = true)
	public List<BidKeywordResponse> listKeywords() {
		return bidKeywordRepository.findAll().stream().map(BidKeywordResponse::from).toList();
	}

	@Override
	@Transactional
	public BidKeywordResponse addKeyword(String keyword) {
		if (bidKeywordRepository.count() >= MAX_KEYWORDS) {
			throw new IllegalArgumentException("키워드는 최대 " + MAX_KEYWORDS + "개까지 등록할 수 있습니다.");
		}
		if (bidKeywordRepository.existsByKeyword(keyword)) {
			throw new IllegalArgumentException("이미 등록된 키워드입니다: " + keyword);
		}
		// 사전 체크와 저장 사이엔 시간차가 있어, 같은 키워드를 거의 동시에 두 번 추가 요청하면
		// (예: 반응이 없어 보여 버튼을 다시 누른 경우) 둘 다 체크를 통과한 뒤 하나만 저장되고
		// 나머지 하나는 DB 유니크 제약에 걸릴 수 있다 — 그 경우도 같은 안내 메시지로 처리한다.
		try {
			return BidKeywordResponse.from(bidKeywordRepository.save(new BidKeyword(keyword)));
		} catch (DataIntegrityViolationException e) {
			throw new IllegalArgumentException("이미 등록된 키워드입니다: " + keyword);
		}
	}

	@Override
	@Transactional
	public void removeKeyword(Long id) {
		bidKeywordRepository.deleteById(id);
	}

	private String buildCompanyProfile() {
		// CERTIFICATE files are short and high-signal (특허/인증 등급), so they go first —
		// if the combined text still exceeds the cap, it's the long DOMAIN_INTRO narrative
		// that gets truncated, not the certificates.
		String profile = companyFileRepository.findAll().stream()
				.filter(file -> file.getCategory() == FileCategory.DOMAIN_INTRO || file.getCategory() == FileCategory.CERTIFICATE)
				.sorted(Comparator.comparing(file -> file.getCategory() == FileCategory.CERTIFICATE ? 0 : 1))
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
