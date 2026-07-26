package com.allforland.automation.repository;

import com.allforland.automation.domain.RequiredItem;
import com.allforland.automation.domain.RequirementSet;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequiredItemRepository extends JpaRepository<RequiredItem, Long> {

	List<RequiredItem> findAllByRequirementSet(RequirementSet requirementSet);
}
