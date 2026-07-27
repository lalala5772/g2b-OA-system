package com.allforland.automation.service;

import com.allforland.automation.dto.BidKeywordResponse;
import com.allforland.automation.dto.BidNoticeResponse;
import com.allforland.automation.dto.BidScanSummaryResponse;
import java.time.LocalDate;
import java.util.List;

public interface BidService {

	/** startDate/endDate 둘 다 비우면 최근 7일을 사용. 둘 중 하나만 주어지거나 시작일이 종료일보다 늦으면 예외. */
	BidScanSummaryResponse triggerScan(LocalDate startDate, LocalDate endDate);

	/** 조회기간 내 announceDate를 가진 적격 공고만 반환 — startDate/endDate 둘 다 비우면 최근 7일. */
	List<BidNoticeResponse> recentEligible(LocalDate startDate, LocalDate endDate);

	BidNoticeResponse getById(Long id);

	List<BidKeywordResponse> listKeywords();

	BidKeywordResponse addKeyword(String keyword);

	void removeKeyword(Long id);
}
