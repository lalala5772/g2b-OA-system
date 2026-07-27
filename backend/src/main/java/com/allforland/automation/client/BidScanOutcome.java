package com.allforland.automation.client;

import java.time.LocalDate;
import java.util.List;

public record BidScanOutcome(
		List<BidScanResult> results, int fetched, LocalDate rangeStart, LocalDate rangeEnd, int judged, int unjudged) {

	public static BidScanOutcome empty(LocalDate rangeStart, LocalDate rangeEnd) {
		return new BidScanOutcome(List.of(), 0, rangeStart, rangeEnd, 0, 0);
	}
}
