package com.allforland.automation.scheduler;

import com.allforland.automation.common.BidScanWindow;
import com.allforland.automation.dto.BidScanSummaryResponse;
import com.allforland.automation.service.BidService;
import java.time.LocalDate;
import java.time.ZoneId;
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
			BidScanWindow.Window window = BidScanWindow.current();
			ZoneId zone = ZoneId.systemDefault();
			LocalDate startDate = window.start().atZone(zone).toLocalDate();
			LocalDate endDate = window.end().atZone(zone).toLocalDate();

			BidScanSummaryResponse summary = bidService.triggerScan(startDate, endDate);
			log.info(
					"나라장터 스캔 완료: fetched={}, matched={}, new={}, eligible={}",
					summary.fetched(),
					summary.matched(),
					summary.newNotices(),
					summary.eligibleCount());
		} catch (Exception ex) {
			log.error("나라장터 스캔 실패", ex);
		}
	}
}
