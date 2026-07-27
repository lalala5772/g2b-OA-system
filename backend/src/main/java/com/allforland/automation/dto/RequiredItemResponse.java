package com.allforland.automation.dto;

import com.allforland.automation.domain.RequiredItem;

public record RequiredItemResponse(
		Long id, String itemName, String description, boolean matched, String matchReason, String matchedFileName) {

	public static RequiredItemResponse from(RequiredItem item) {
		return new RequiredItemResponse(
				item.getId(),
				item.getItemName(),
				item.getDescription(),
				item.isMatched(),
				item.getMatchReason(),
				item.getMatchedCompanyFile() != null ? item.getMatchedCompanyFile().getFileName() : null);
	}
}
