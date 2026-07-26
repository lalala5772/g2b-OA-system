package com.allforland.automation.dto;

import com.allforland.automation.domain.IdeaRequestStatus;
import java.util.List;

public record IdeaGenerateResponse(Long requestId, IdeaRequestStatus status, List<ContestIdeaResponse> ideas) {
}
