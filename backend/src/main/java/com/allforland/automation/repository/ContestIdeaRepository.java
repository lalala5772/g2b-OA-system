package com.allforland.automation.repository;

import com.allforland.automation.domain.ContestIdea;
import com.allforland.automation.domain.IdeaRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestIdeaRepository extends JpaRepository<ContestIdea, Long> {

	List<ContestIdea> findAllByIdeaRequestOrderByRelevanceScoreDesc(IdeaRequest ideaRequest);

	List<ContestIdea> findTop20ByOrderByGeneratedAtDesc();
}
