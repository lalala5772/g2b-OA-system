package com.allforland.automation.common;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * The platform always looks at a fixed daily window — yesterday 10:00 to today 10:00 — rather
 * than a rolling "last 24h from now". This keeps a scheduled run and a manual "지금 스캔 실행"
 * click show the same window on the bid page.
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
