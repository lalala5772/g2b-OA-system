package com.allforland.automation.dto;

import com.allforland.automation.domain.BidNotice;
import com.allforland.automation.domain.BidNoticeStatus;
import java.time.Instant;
import java.time.LocalDate;

public record BidNoticeResponse(
		Long id,
		String title,
		String agency,
		String matchedKeyword,
		LocalDate announceDate,
		LocalDate deadline,
		String url,
		Double eligibilityScore,
		String aiSummary,
		String aiJudgement,
		BidNoticeStatus status,
		Instant crawledAt) {

	public static BidNoticeResponse from(BidNotice notice) {
		return new BidNoticeResponse(
				notice.getId(),
				notice.getTitle(),
				notice.getAgency(),
				notice.getMatchedKeyword(),
				notice.getAnnounceDate(),
				notice.getDeadline(),
				notice.getUrl(),
				notice.getEligibilityScore(),
				notice.getAiSummary(),
				notice.getAiJudgement(),
				notice.getStatus(),
				notice.getCrawledAt());
	}
}
