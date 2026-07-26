package com.allforland.automation.repository;

import com.allforland.automation.domain.IdeaRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdeaRequestRepository extends JpaRepository<IdeaRequest, Long> {
}
