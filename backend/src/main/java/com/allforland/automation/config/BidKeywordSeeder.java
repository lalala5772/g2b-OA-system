package com.allforland.automation.config;

import com.allforland.automation.domain.BidKeyword;
import com.allforland.automation.repository.BidKeywordRepository;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** Seeds BID_KEYWORDS from an env var on first boot so Phase 2 is usable without a keyword-management UI. */
@Component
public class BidKeywordSeeder implements ApplicationRunner {

	private final BidKeywordRepository bidKeywordRepository;
	private final String keywordsSeed;

	public BidKeywordSeeder(
			BidKeywordRepository bidKeywordRepository, @Value("${app.bid.keywords-seed}") String keywordsSeed) {
		this.bidKeywordRepository = bidKeywordRepository;
		this.keywordsSeed = keywordsSeed;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (!bidKeywordRepository.findAll().isEmpty() || keywordsSeed == null || keywordsSeed.isBlank()) {
			return;
		}
		Arrays.stream(keywordsSeed.split(","))
				.map(String::trim)
				.filter(keyword -> !keyword.isEmpty())
				.forEach(keyword -> bidKeywordRepository.save(new BidKeyword(keyword)));
	}
}
