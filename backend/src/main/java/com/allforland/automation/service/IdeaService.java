package com.allforland.automation.service;

import com.allforland.automation.dto.ContestIdeaResponse;
import com.allforland.automation.dto.IdeaGenerateRequest;
import com.allforland.automation.dto.IdeaGenerateResponse;
import java.util.List;

public interface IdeaService {

	IdeaGenerateResponse generate(IdeaGenerateRequest request, Long userId);

	List<ContestIdeaResponse> recentIdeas();
}
