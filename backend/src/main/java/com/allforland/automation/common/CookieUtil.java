package com.allforland.automation.common;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;

public class CookieUtil {

	public static final String ACCESS_TOKEN_COOKIE = "ACCESS_TOKEN";

	private CookieUtil() {
	}

	public static void setAccessTokenCookie(HttpServletResponse response, String token, long maxAgeSeconds) {
		ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, token)
				.httpOnly(true)
				.secure(true)
				.sameSite("Lax")
				.path("/")
				.maxAge(maxAgeSeconds)
				.build();
		response.addHeader("Set-Cookie", cookie.toString());
	}

	public static void clearAccessTokenCookie(HttpServletResponse response) {
		ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, "")
				.httpOnly(true)
				.secure(true)
				.sameSite("Lax")
				.path("/")
				.maxAge(0)
				.build();
		response.addHeader("Set-Cookie", cookie.toString());
	}
}
