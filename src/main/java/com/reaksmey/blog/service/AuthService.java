package com.reaksmey.blog.service;

import com.reaksmey.blog.auth.AuthRequest;
import com.reaksmey.blog.auth.AuthResponse;
import com.reaksmey.blog.exception.AuthenticationException;
import com.reaksmey.blog.repository.UserRepository;
import com.reaksmey.blog.config.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthService {

	private final UserRepository userRepository;
	private final AuthenticationManager authManager;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthService(
		UserRepository userRepository,
		AuthenticationManager authManager,
		PasswordEncoder passwordEncoder,
		JwtService jwtService
	) {

		this.userRepository = userRepository;
		this.authManager = authManager;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}


	public AuthResponse authenticate(AuthRequest loginRequest) {

		try {
			UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
				loginRequest.username(),
				loginRequest.password()
			);
			authManager.authenticate(authToken);

			String token = jwtService.generateToken(loginRequest.username());

			return new AuthResponse(token);
		} catch (org.springframework.security.core.AuthenticationException e) {
			throw new AuthenticationException("Invalid username or password");
		}
	}
}
