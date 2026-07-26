package com.allforland.automation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bid_notices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BidNotice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "external_bid_no", nullable = false, unique = true)
	private String externalBidNo;

	@Column(nullable = false)
	private String title;

	private String agency;

	@Column(name = "matched_keyword")
	private String matchedKeyword;

	@Column(name = "announce_date")
	private LocalDate announceDate;

	private LocalDate deadline;

	private String url;

	@Column(name = "eligibility_score")
	private Double eligibilityScore;

	@Column(name = "ai_judgement", columnDefinition = "TEXT")
	private String aiJudgement;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private BidNoticeStatus status = BidNoticeStatus.DETECTED;

	@Column(name = "crawled_at", nullable = false)
	private Instant crawledAt;

	public BidNotice(
			String externalBidNo,
			String title,
			String agency,
			String matchedKeyword,
			LocalDate announceDate,
			LocalDate deadline,
			String url) {
		this.externalBidNo = externalBidNo;
		this.title = title;
		this.agency = agency;
		this.matchedKeyword = matchedKeyword;
		this.announceDate = announceDate;
		this.deadline = deadline;
		this.url = url;
		this.status = BidNoticeStatus.DETECTED;
		this.crawledAt = Instant.now();
	}

	public void applyEligibility(Double score, String judgement) {
		this.eligibilityScore = score;
		this.aiJudgement = judgement;
	}

	public void markNotified() {
		this.status = BidNoticeStatus.NOTIFIED;
	}

	public void markIgnored() {
		this.status = BidNoticeStatus.IGNORED;
	}
}
