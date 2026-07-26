package com.allforland.automation.controller;

import com.allforland.automation.common.ApiResponse;
import com.allforland.automation.dto.BidKeywordResponse;
import com.allforland.automation.dto.BidNoticeResponse;
import com.allforland.automation.dto.BidScanSummaryResponse;
import com.allforland.automation.service.BidService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bids")
public class BidController {

	private final BidService bidService;

	public BidController(BidService bidService) {
		this.bidService = bidService;
	}

	@GetMapping("/recent")
	public ApiResponse<List<BidNoticeResponse>> recent() {
		return ApiResponse.ok(bidService.recentNotices());
	}

	@PostMapping("/scan-now")
	public ApiResponse<BidScanSummaryResponse> scanNow() {
		return ApiResponse.ok(bidService.triggerScan());
	}

	@GetMapping("/keywords")
	public ApiResponse<List<BidKeywordResponse>> keywords() {
		return ApiResponse.ok(bidService.listKeywords());
	}

	@PostMapping("/keywords")
	public ApiResponse<BidKeywordResponse> addKeyword(@RequestBody Map<String, String> body) {
		String keyword = body.get("keyword");
		if (keyword == null || keyword.isBlank()) {
			throw new IllegalArgumentException("키워드를 입력해주세요.");
		}
		return ApiResponse.ok(bidService.addKeyword(keyword.trim()));
	}

	@DeleteMapping("/keywords/{id}")
	public ApiResponse<Void> removeKeyword(@PathVariable Long id) {
		bidService.removeKeyword(id);
		return ApiResponse.ok(null);
	}
}
