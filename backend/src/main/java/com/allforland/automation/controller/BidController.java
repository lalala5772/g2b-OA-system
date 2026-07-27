package com.allforland.automation.controller;

import com.allforland.automation.common.ApiResponse;
import com.allforland.automation.dto.BidKeywordResponse;
import com.allforland.automation.dto.BidNoticeResponse;
import com.allforland.automation.dto.BidScanRequest;
import com.allforland.automation.dto.BidScanSummaryResponse;
import com.allforland.automation.service.BidService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bids")
public class BidController {

	private final BidService bidService;

	public BidController(BidService bidService) {
		this.bidService = bidService;
	}

	@GetMapping("/eligible")
	public ApiResponse<List<BidNoticeResponse>> eligible(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
		return ApiResponse.ok(bidService.recentEligible(startDate, endDate));
	}

	@GetMapping("/{id}")
	public ApiResponse<BidNoticeResponse> get(@PathVariable Long id) {
		return ApiResponse.ok(bidService.getById(id));
	}

	@PostMapping("/scan-now")
	public ApiResponse<BidScanSummaryResponse> scanNow(@RequestBody(required = false) BidScanRequest request) {
		LocalDate startDate = request != null ? request.startDate() : null;
		LocalDate endDate = request != null ? request.endDate() : null;
		return ApiResponse.ok(bidService.triggerScan(startDate, endDate));
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
