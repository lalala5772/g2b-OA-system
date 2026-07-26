package com.allforland.automation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bid_keywords")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BidKeyword {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String keyword;

	@Column(nullable = false)
	private boolean active = true;

	public BidKeyword(String keyword) {
		this.keyword = keyword;
		this.active = true;
	}
}
