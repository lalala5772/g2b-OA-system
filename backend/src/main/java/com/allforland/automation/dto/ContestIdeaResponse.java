package com.allforland.automation.dto;

import com.allforland.automation.domain.ContestIdea;
import java.time.Instant;

public record ContestIdeaResponse(Long id, String ideaTitle, String ideaContent, Double relevanceScore, Instant generatedAt) {

	public static ContestIdeaResponse from(ContestIdea idea) {
		return new ContestIdeaResponse(
				idea.getId(), idea.getIdeaTitle(), idea.getIdeaContent(), idea.getRelevanceScore(), idea.getGeneratedAt());
	}
}
