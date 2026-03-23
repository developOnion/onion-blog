package com.reaksmey.blog.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reaksmey.blog.config.JwtAuthenticationEntryPoint;
import com.reaksmey.blog.config.JwtService;
import com.reaksmey.blog.exception.AuthenticationException;
import com.reaksmey.blog.exception.ResourceAlreadyExistsException;
import com.reaksmey.blog.exception.ResourceNotFoundException;
import com.reaksmey.blog.user.User;
import com.reaksmey.blog.user.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import java.io.IOException;
import java.util.Arrays;

import com.reaksmey.blog.token.Token;
import com.reaksmey.blog.token.TokenRepository;
import com.reaksmey.blog.token.TokenType;

@Slf4j
@Service
public class AuthService {

	private final UserRepository userRepository;
	private final AuthenticationManager authManager;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	private final TokenRepository tokenRepository;

	public AuthService(
		UserRepository userRepository,
		AuthenticationManager authManager,
		PasswordEncoder passwordEncoder,
		JwtService jwtService,
		JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
		TokenRepository tokenRepository
	) {

		this.userRepository = userRepository;
		this.authManager = authManager;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
		this.tokenRepository = tokenRepository;
	}

	public AuthResponse authenticate(AuthRequest loginRequest, HttpServletResponse response) {

		try {
			UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
				loginRequest.username(),
				loginRequest.password()
			);
			authManager.authenticate(authToken);

			var user = userRepository.findByUsername(loginRequest.username())
				.orElseThrow(() -> new AuthenticationException("User not found"));

			UserPrincipal principal = new UserPrincipal(user);
			var token = jwtService.generateToken(principal);
			var refreshToken = jwtService.generateRefreshToken(principal);

			revokeAllUserTokens(user);
			saveUserToken(user, token);

			setRefreshTokenCookie(response, refreshToken);

			return new AuthResponse(token);
		} catch (org.springframework.security.core.AuthenticationException e) {
			throw new AuthenticationException("Invalid username or password");
		}
	}

	public AuthResponse register(AuthRequest registerRequest, HttpServletResponse response) {
		
		if (userRepository.findByUsername(registerRequest.username()).isPresent()) {
			throw new ResourceAlreadyExistsException("User with this username already exists");
		}

		var user = User.builder()
			.username(registerRequest.username())
			.password(passwordEncoder.encode(registerRequest.password()))
			.build();

		var savedUser = userRepository.save(user);

		var principal = new UserPrincipal(savedUser);
		var token = jwtService.generateToken(principal);
		var refreshToken = jwtService.generateRefreshToken(principal);

		saveUserToken(savedUser, token);

		setRefreshTokenCookie(response, refreshToken);

		return new AuthResponse(token);
	}

	private void saveUserToken(User user, String jwtToken) {
		var token = Token.builder()
			.user(user)
			.token(jwtToken)
			.tokenType(TokenType.BEARER)
			.expired(false)
			.revoked(false)
			.build();
		tokenRepository.save(token);
	}

	private void revokeAllUserTokens(User user) {
		var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
		if (validUserTokens.isEmpty())
			return;
		validUserTokens.forEach(token -> {
			token.setExpired(true);
			token.setRevoked(true);
		});
		tokenRepository.saveAll(validUserTokens);
	}

	public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {

		String refreshToken = null;
		if (request.getCookies() != null) {
			refreshToken = Arrays.stream(request.getCookies())
				.filter(cookie -> "refresh_token".equals(cookie.getName()))
				.map(Cookie::getValue)
				.findFirst()
				.orElse(null);
		}

		if (refreshToken == null) {
			log.info("Missing refresh token cookie");
			return;
		}

		try {
			final String username = jwtService.extractUsername(refreshToken);

			if (username != null) {

				UserPrincipal userPrincipal = new UserPrincipal(
					userRepository.findByUsername(username)
						.orElseThrow(() -> new ResourceNotFoundException("User does not exists"))
				);

				if (jwtService.isValidToken(refreshToken, userPrincipal)) {
					var accessToken = jwtService.generateToken(userPrincipal);
					
					// Revoke old tokens and save new access token
					User user = userPrincipal.user();
					revokeAllUserTokens(user);
					saveUserToken(user, accessToken);

					var authResponse = new AuthResponse(accessToken);

					new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
				}
			}
		} catch (JwtException e) {
			SecurityContextHolder.clearContext();
			String clientMessage = jwtService.mapExceptionToClientMessage(e);
			log.debug("JWT error: {}", e.getMessage());
			jwtAuthenticationEntryPoint.commence(request, response, new org.springframework.security.core.AuthenticationException(clientMessage) {});
		}
	}

	private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
		ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
			.httpOnly(true)
			.secure(false) // Set to true in production with HTTPS
			.path("/")
			.maxAge(jwtService.getRefreshExpirationTime() / 1000)
			.sameSite("Lax")
			.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}
}
