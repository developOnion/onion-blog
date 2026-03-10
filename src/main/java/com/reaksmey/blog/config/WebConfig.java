package com.reaksmey.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

@Configuration
public class WebConfig {

	@Bean
	public PageableHandlerMethodArgumentResolverCustomizer pageableCustomizer() {
		return pageableResolver -> {
			pageableResolver.setOneIndexedParameters(false);
			pageableResolver.setFallbackPageable(PageRequest.of(0, 10));
			pageableResolver.setMaxPageSize(100);
		};
	}
}
