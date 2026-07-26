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
@Table(name = "contest_ideas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContestIdea {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "idea_request_id", nullable = false)
	private IdeaRequest ideaRequest;

	@Column(name = "idea_title", nullable = false)
	private String ideaTitle;

	@Column(name = "idea_content", nullable = false, columnDefinition = "TEXT")
	private String ideaContent;

	@Column(name = "relevance_score")
	private Double relevanceScore;

	@Column(name = "llm_model")
	private String llmModel;

	@Column(name = "generated_at", nullable = false)
	private Instant generatedAt;

	public ContestIdea(IdeaRequest ideaRequest, String ideaTitle, String ideaContent, Double relevanceScore, String llmModel) {
		this.ideaRequest = ideaRequest;
		this.ideaTitle = ideaTitle;
		this.ideaContent = ideaContent;
		this.relevanceScore = relevanceScore;
		this.llmModel = llmModel;
		this.generatedAt = Instant.now();
	}
}
