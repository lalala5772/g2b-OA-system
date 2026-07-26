package com.allforland.automation.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BidScanResult(
		@JsonProperty("external_bid_no") String externalBidNo,
		String title,
		String agency,
		@JsonProperty("matched_keyword") String matchedKeyword,
		@JsonProperty("announce_date") String announceDate,
		String deadline,
		String url,
		@JsonProperty("eligibility_score") Double eligibilityScore,
		@JsonProperty("ai_judgement") String aiJudgement,
		boolean eligible) {
}
