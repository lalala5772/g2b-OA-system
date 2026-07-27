package com.allforland.automation.repository;

import com.allforland.automation.domain.BidKeyword;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BidKeywordRepository extends JpaRepository<BidKeyword, Long> {

	List<BidKeyword> findAllByActiveTrue();

	boolean existsByKeyword(String keyword);
}
