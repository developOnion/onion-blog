package com.reaksmey.blog.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "HealthController", description = "Endpoint for health check")
public class HealthController {

	@GetMapping("/health")
	public String health() {
		return "OK";
	}
}
