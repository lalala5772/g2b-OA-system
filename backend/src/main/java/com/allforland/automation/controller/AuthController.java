package com.allforland.automation.controller;

import com.allforland.automation.common.ApiResponse;
import com.allforland.automation.common.AuthenticatedPrincipal;
import com.allforland.automation.common.CookieUtil;
import com.allforland.automation.domain.User;
import com.allforland.automation.dto.UserResponse;
import com.allforland.automation.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final UserService userService;

	public AuthController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/me")
	public ApiResponse<UserResponse> me(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
		User user = userService.getById(principal.userId());
		return ApiResponse.ok(UserResponse.from(user));
	}

	@PostMapping("/logout")
	public ApiResponse<Void> logout(HttpServletResponse response) {
		CookieUtil.clearAccessTokenCookie(response);
		return ApiResponse.ok(null);
	}
}
