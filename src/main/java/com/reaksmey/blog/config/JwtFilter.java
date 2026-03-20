package com.reaksmey.blog.config;

import com.reaksmey.blog.auth.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UserDetailsServiceImpl userDetailsService;

	public JwtFilter(
		JwtService jwtService,
		UserDetailsServiceImpl userDetailsService
	) {

		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(
		@NonNull HttpServletRequest request,
		@NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain
	) throws ServletException, IOException {

		final String path = request.getServletPath();
		log.info("Incoming request to {}", path);

		if (path.startsWith("/auth")
			|| path.startsWith("/v3/api-docs")
			|| path.startsWith("/swagger-ui")
			|| path.equals("/swagger-ui.html")
		) {
			log.info("Request to {} - skipping JWT filter", request.getServletPath());
			log.info("Skipping JWT filter for auth endpoint");
			filterChain.doFilter(request, response);
			return;
		}

		final String authHeader = request.getHeader("Authorization");
		final String token;
		final String username;

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			log.info("Missing or invalid Authorization header");
			filterChain.doFilter(request, response);
			return;
		}

		try {
			token = authHeader.substring(7);
			username = jwtService.extractUsername(token);

			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

				UserDetails userDetails = userDetailsService.loadUserByUsername(username);

				if (jwtService.isValidToken(token, userDetails)) {

					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
						userDetails,
						null,
						userDetails.getAuthorities()
					);
					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authToken);
				}
			}

			filterChain.doFilter(request, response);
		} catch (SignatureException e) {
			handleJwtException(response, "Invalid JWT signature");
		} catch (ExpiredJwtException e) {
			handleJwtException(response, "JWT token has expired");
		} catch (MalformedJwtException e) {
			handleJwtException(response, "Invalid JWT token");
		}
	}

	private void handleJwtException(
		HttpServletResponse response,
		String message
	) throws IOException {

		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json");
		response.getWriter().write(
			"{\"timestamp\":\"" + LocalDateTime.now() + "\"," +
				"\"status\":401," +
				"\"message\":\"" + message + "\"}"
		);
	}
}
