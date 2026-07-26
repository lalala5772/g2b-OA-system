package com.allforland.automation.service;

import com.allforland.automation.dto.BidKeywordResponse;
import com.allforland.automation.dto.BidNoticeResponse;
import com.allforland.automation.dto.BidScanSummaryResponse;
import java.util.List;

public interface BidService {

	BidScanSummaryResponse triggerScan();

	List<BidNoticeResponse> recentNotices();

	List<BidKeywordResponse> listKeywords();

	BidKeywordResponse addKeyword(String keyword);

	void removeKeyword(Long id);
}
