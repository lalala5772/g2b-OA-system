package com.allforland.automation.dto;

import com.allforland.automation.domain.RequirementSetStatus;
import java.util.List;

public record EvidenceAnalysisResponse(
		Long requirementSetId,
		RequirementSetStatus status,
		List<RequiredItemResponse> items,
		Long zipExportId,
		int matchedCount,
		int missingCount) {
}
