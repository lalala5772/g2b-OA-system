package com.allforland.automation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "bid_notice_id", nullable = false)
	private BidNotice bidNotice;

	@Column(nullable = false)
	private String channel;

	@Column(nullable = false)
	private String status;

	@Column(name = "response_code")
	private Integer responseCode;

	@Column(name = "sent_at", nullable = false)
	private Instant sentAt;

	public NotificationLog(BidNotice bidNotice, String channel, String status, Integer responseCode) {
		this.bidNotice = bidNotice;
		this.channel = channel;
		this.status = status;
		this.responseCode = responseCode;
		this.sentAt = Instant.now();
	}
}
