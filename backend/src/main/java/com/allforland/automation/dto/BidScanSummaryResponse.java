package com.allforland.automation.dto;

import java.time.LocalDate;

public record BidScanSummaryResponse(
		int fetched,
		int matched,
		int newNotices,
		int eligibleCount,
		int judged,
		int unjudged,
		LocalDate rangeStart,
		LocalDate rangeEnd) {
}
