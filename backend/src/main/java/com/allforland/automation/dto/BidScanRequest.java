package com.allforland.automation.dto;

import java.time.LocalDate;

/** 둘 다 없으면 ai-engine 쪽 기본값(최근 7일)을 사용. */
public record BidScanRequest(LocalDate startDate, LocalDate endDate) {
}
