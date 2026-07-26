package com.allforland.automation.common;

public record NotImplementedResponse(String status, String message) {

	public static NotImplementedResponse of(String featureName) {
		return new NotImplementedResponse(
				"not_implemented",
				featureName + " 기능은 Phase 2 이후에 제공될 예정입니다.");
	}
}
