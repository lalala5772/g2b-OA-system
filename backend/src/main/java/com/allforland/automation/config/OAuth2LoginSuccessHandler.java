package com.allforland.automation.config;

import com.allforland.automation.common.CookieUtil;
import com.allforland.automation.common.JwtTokenProvider;
import com.allforland.automation.domain.User;
import com.allforland.automation.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

	private final UserService userService;
	private final JwtTokenProvider jwtTokenProvider;
	private final String frontendUrl;
	private final boolean cookieSecure;

	public OAuth2LoginSuccessHandler(
			UserService userService,
			JwtTokenProvider jwtTokenProvider,
			@Value("${app.frontend-url}") String frontendUrl,
			@Value("${app.jwt.cookie-secure}") boolean cookieSecure) {
		this.userService = userService;
		this.jwtTokenProvider = jwtTokenProvider;
		this.frontendUrl = frontendUrl;
		this.cookieSecure = cookieSecure;
	}

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException {
		OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
		String googleId = oAuth2User.getAttribute("sub");
		String email = oAuth2User.getAttribute("email");
		String name = oAuth2User.getAttribute("name");
		String picture = oAuth2User.getAttribute("picture");

		User user = userService.findOrCreateByGoogle(googleId, email, name, picture);
		String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());
		CookieUtil.setAccessTokenCookie(response, token, jwtTokenProvider.expirationSeconds(), cookieSecure);

		response.sendRedirect(frontendUrl + "/dashboard");
	}
}
