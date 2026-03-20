package com.reaksmey.blog.controller;

import com.reaksmey.blog.auth.AuthRequest;
import com.reaksmey.blog.auth.AuthResponse;
import com.reaksmey.blog.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
		@Valid @RequestBody AuthRequest loginRequest
	) {

		AuthResponse response = authService.authenticate(loginRequest);

		return ResponseEntity.ok().body(response);
	}
}
