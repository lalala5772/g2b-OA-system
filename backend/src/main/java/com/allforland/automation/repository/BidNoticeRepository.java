package com.allforland.automation.repository;

import com.allforland.automation.domain.BidNotice;
import com.allforland.automation.domain.BidNoticeStatus;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BidNoticeRepository extends JpaRepository<BidNotice, Long> {

	Optional<BidNotice> findByExternalBidNo(String externalBidNo);

	List<BidNotice> findTop50ByStatusAndAnnounceDateBetweenAndMatchedKeywordInOrderByCrawledAtDesc(
			BidNoticeStatus status, LocalDate announceDateFrom, LocalDate announceDateTo, Collection<String> matchedKeywords);

	long countByStatus(BidNoticeStatus status);
}
