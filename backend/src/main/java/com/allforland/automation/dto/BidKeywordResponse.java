package com.allforland.automation.dto;

import com.allforland.automation.domain.BidKeyword;

public record BidKeywordResponse(Long id, String keyword, boolean active) {

	public static BidKeywordResponse from(BidKeyword keyword) {
		return new BidKeywordResponse(keyword.getId(), keyword.getKeyword(), keyword.isActive());
	}
}
