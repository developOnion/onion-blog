package com.reaksmey.blog.config;

import com.reaksmey.blog.auth.UserDetailsServiceImpl;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import com.reaksmey.blog.token.TokenRepository;

@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UserDetailsServiceImpl userDetailsService;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	private final TokenRepository tokenRepository;

	public JwtFilter(
		JwtService jwtService,
		UserDetailsServiceImpl userDetailsService,
		JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
		TokenRepository tokenRepository
	) {

		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
		this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
		this.tokenRepository = tokenRepository;
	}

	@Override
	protected void doFilterInternal(
		@NonNull HttpServletRequest request,
		@NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain
	) throws ServletException, IOException {

		final String authHeader = request.getHeader("Authorization");
		final String accessToken;
		final String username;

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			log.info("Missing or invalid Authorization header");
			filterChain.doFilter(request, response);
			return;
		}

		try {
			accessToken = authHeader.substring(7);
			username = jwtService.extractUsername(accessToken);

			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

				UserDetails userDetails = userDetailsService.loadUserByUsername(username);

				var isTokenValid = tokenRepository.findByToken(accessToken)
					.map(t -> !t.isExpired() && !t.isRevoked())
					.orElse(false);

				if (jwtService.isValidToken(accessToken, userDetails) && isTokenValid) {

					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
						userDetails,
						null,
						userDetails.getAuthorities()
					);
					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authToken);
				} else {
					log.debug("Token is invalid or revoked for user: {}", username);
					jwtAuthenticationEntryPoint.commence(request, response, new AuthenticationException("Token is invalid or has been revoked") {});
					return;
				}
			}

			filterChain.doFilter(request, response);
		} catch (JwtException e) {
			SecurityContextHolder.clearContext();
			String clientMessage = jwtService.mapExceptionToClientMessage(e);
			log.debug("JWT error: {}", e.getMessage());
			jwtAuthenticationEntryPoint.commence(request, response, new AuthenticationException(clientMessage) {});
		}
	}
}
