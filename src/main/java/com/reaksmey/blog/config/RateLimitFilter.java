package com.reaksmey.blog.config;

import com.reaksmey.blog.exception.TooManyRequestsException;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

	private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
	private final HandlerExceptionResolver resolver;

	public RateLimitFilter(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
		this.resolver = resolver;
	}

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		@NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain
	) throws ServletException, IOException {

		String clientIp = request.getRemoteAddr();

		// If behind a proxy, use X-Forwarded-For
		String xForwardedFor = request.getHeader("X-Forwarded-For");
		if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
			clientIp = xForwardedFor.split(",")[0].trim();
		}

		Bucket bucket = buckets.computeIfAbsent(clientIp, this::createNewBucket);

		if (bucket.tryConsume(1)) {
			filterChain.doFilter(request, response);
		} else {
			resolver.resolveException(request, response, null, new TooManyRequestsException("You have exhausted your API request quota. Please try again later."));
		}
	}

	private Bucket createNewBucket(String key) {
		// 100 requests per minute
		return Bucket.builder()
			.addLimit(limit -> limit
				.capacity(100) // burst capacity
				.refillGreedy(100, Duration.ofMinutes(1)) // refill 100 tokens every minute
			)
			.build();
	}
}
