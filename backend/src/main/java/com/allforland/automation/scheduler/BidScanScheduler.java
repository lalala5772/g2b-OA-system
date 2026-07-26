package com.allforland.automation.scheduler;

import com.allforland.automation.dto.BidScanSummaryResponse;
import com.allforland.automation.service.BidService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BidScanScheduler {

	private static final Logger log = LoggerFactory.getLogger(BidScanScheduler.class);

	private final BidService bidService;

	public BidScanScheduler(BidService bidService) {
		this.bidService = bidService;
	}

	@Scheduled(cron = "${app.bid.scan-cron}")
	public void scanDaily() {
		try {
			BidScanSummaryResponse summary = bidService.triggerScan();
			log.info(
					"나라장터 스캔 완료: fetched={}, new={}, eligible={}",
					summary.fetched(),
					summary.newNotices(),
					summary.eligibleCount());
		} catch (Exception ex) {
			log.error("나라장터 스캔 실패", ex);
		}
	}
}
