package com.allforland.automation.config;

import com.allforland.automation.common.AuthenticatedPrincipal;
import com.allforland.automation.common.CookieUtil;
import com.allforland.automation.common.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;

	public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		extractToken(request)
				.flatMap(jwtTokenProvider::parse)
				.ifPresent(principal -> {
					var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + principal.role()));
					var authentication =
							new UsernamePasswordAuthenticationToken(principal, null, authorities);
					SecurityContextHolder.getContext().setAuthentication(authentication);
				});
		filterChain.doFilter(request, response);
	}

	private Optional<String> extractToken(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return Optional.empty();
		}
		for (Cookie cookie : cookies) {
			if (CookieUtil.ACCESS_TOKEN_COOKIE.equals(cookie.getName())) {
				return Optional.ofNullable(cookie.getValue());
			}
		}
		return Optional.empty();
	}
}
