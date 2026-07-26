package com.allforland.automation.config;

import com.allforland.automation.common.JwtTokenProvider;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
	private final JwtTokenProvider jwtTokenProvider;
	private final String frontendUrl;

	public SecurityConfig(
			OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler,
			JwtTokenProvider jwtTokenProvider,
			@Value("${app.frontend-url}") String frontendUrl) {
		this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
		this.jwtTokenProvider = jwtTokenProvider;
		this.frontendUrl = frontendUrl;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/oauth2/**", "/login/**", "/actuator/health").permitAll()
						.requestMatchers("/api/**").authenticated()
						.anyRequest().permitAll())
				.oauth2Login(oauth2 -> oauth2.successHandler(oAuth2LoginSuccessHandler))
				.exceptionHandling(handling -> handling.authenticationEntryPoint(
						(request, response, ex) -> response.sendError(401, "인증이 필요합니다.")))
				.addFilterBefore(
						new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	private CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of(frontendUrl));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
