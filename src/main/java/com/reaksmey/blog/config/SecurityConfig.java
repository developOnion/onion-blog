package com.reaksmey.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

	public static final String[] WHITE_LIST_URL = {
		"/auth/**",
		"/v3/api-docs/**",
		"/swagger-ui/**",
		"/swagger-ui.html",
		"/actuator/health",
	};

	private final JwtFilter jwtFilter;
	private final RateLimitFilter rateLimitFilter;
	private final AuthenticationProvider authenticationProvider;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	private final LogoutService logoutService;

	public SecurityConfig(
		JwtFilter jwtFilter,
		RateLimitFilter rateLimitFilter,
		AuthenticationProvider authenticationProvider,
		JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
		LogoutService logoutService
	) {

		this.jwtFilter = jwtFilter;
		this.rateLimitFilter = rateLimitFilter;
		this.authenticationProvider = authenticationProvider;
		this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
		this.logoutService = logoutService;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(
		HttpSecurity httpSecurity
	) {

		return httpSecurity
			.cors(withDefaults())
			.csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(WHITE_LIST_URL).permitAll()
				.requestMatchers(HttpMethod.GET, "/posts/**").permitAll()
				.anyRequest().authenticated()
			)
			.sessionManagement(
				session -> session
					.sessionCreationPolicy(STATELESS)
			)
			.exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
			.authenticationProvider(authenticationProvider)
			.addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
			.logout(logout ->
				logout.logoutUrl("/auth/logout")
					.addLogoutHandler(logoutService)
					.logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext())
			)
			.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(List.of("http://localhost:5173/"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
		configuration.setExposedHeaders(List.of("Authorization"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
