package com.reaksmey.blog.auth;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/auth")
@Tag(name = "AuthController", description = "Endpoints for user authentication")
public class AuthController {

	private final AuthService authService;

	public AuthController(
		AuthService authService
	) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(
		@Valid @RequestBody AuthRequest loginRequest,
		HttpServletResponse response
	) {
		AuthResponse authResponse = authService.authenticate(loginRequest, response);
		return ResponseEntity.ok().body(authResponse);
	}

	@PostMapping("/refresh-token")
	public void refreshToken(
		HttpServletRequest request,
		HttpServletResponse response
	) throws IOException {
		authService.refreshToken(request, response);
	}

	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register(
		@Valid @RequestBody AuthRequest registerRequest,
		HttpServletResponse response
	) {
		AuthResponse authResponse = authService.register(registerRequest, response);
		return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
	}
}
