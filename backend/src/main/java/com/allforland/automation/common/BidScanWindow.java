package com.allforland.automation.common;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Fixed daily window (yesterday 10:00 -> today 10:00) used only as the default range for the
 * automatic {@code @Scheduled} bid scan. Manual scans from the UI take an explicit date range
 * instead (see {@code BidService#triggerScan}) and default to the last 7 days when omitted.
 */
public class BidScanWindow {

	private static final int BOUNDARY_HOUR = 10;

	private BidScanWindow() {
	}

	public record Window(Instant start, Instant end) {
	}

	public static Window current() {
		return current(Clock.systemDefaultZone());
	}

	public static Window current(Clock clock) {
		ZoneId zone = clock.getZone();
		LocalDateTime now = LocalDateTime.now(clock);
		LocalDateTime todayBoundary = now.toLocalDate().atTime(BOUNDARY_HOUR, 0);
		LocalDateTime end = now.isBefore(todayBoundary) ? todayBoundary.minusDays(1) : todayBoundary;
		LocalDateTime start = end.minusDays(1);
		return new Window(start.atZone(zone).toInstant(), end.atZone(zone).toInstant());
	}
}
