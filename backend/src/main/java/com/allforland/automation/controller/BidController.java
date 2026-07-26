package com.allforland.automation.controller;

import com.allforland.automation.common.ApiResponse;
import com.allforland.automation.common.NotImplementedResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bids")
public class BidController {

	@GetMapping("/recent")
	public ApiResponse<NotImplementedResponse> recent() {
		return ApiResponse.ok(NotImplementedResponse.of("나라장터 자동화"));
	}
}
