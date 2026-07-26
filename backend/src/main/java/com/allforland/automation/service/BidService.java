package com.allforland.automation.service;

import com.allforland.automation.dto.BidKeywordResponse;
import com.allforland.automation.dto.BidScanSummaryResponse;
import com.allforland.automation.dto.BidWindowResponse;
import java.util.List;

public interface BidService {

	BidScanSummaryResponse triggerScan();

	/** 어제 10:00 ~ 오늘 10:00 사이 감지된 적격 공고. */
	BidWindowResponse eligibleInCurrentWindow();

	List<BidKeywordResponse> listKeywords();

	BidKeywordResponse addKeyword(String keyword);

	void removeKeyword(Long id);
}
