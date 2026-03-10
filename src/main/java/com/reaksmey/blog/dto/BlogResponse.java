package com.reaksmey.blog.dto;

import java.util.UUID;

public record BlogResponse(
	UUID id,
	String title,
	String slug,
	String content,
	String excerpt,
	String author,
	String featuredImageUrl,
	String status
) {
}
