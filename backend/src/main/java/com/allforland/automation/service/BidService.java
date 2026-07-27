package com.allforland.automation.service;

import com.allforland.automation.dto.BidKeywordResponse;
import com.allforland.automation.dto.BidNoticeResponse;
import com.allforland.automation.dto.BidScanSummaryResponse;
import java.time.LocalDate;
import java.util.List;

public interface BidService {

	/** startDate/endDate null이면 ai-engine 쪽 기본값(최근 7일)을 사용. */
	BidScanSummaryResponse triggerScan(LocalDate startDate, LocalDate endDate);

	List<BidNoticeResponse> recentEligible();

	BidNoticeResponse getById(Long id);

	List<BidKeywordResponse> listKeywords();

	BidKeywordResponse addKeyword(String keyword);

	void removeKeyword(Long id);
}
