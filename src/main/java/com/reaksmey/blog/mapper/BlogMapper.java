package com.reaksmey.blog.mapper;

import com.reaksmey.blog.dto.BlogResponse;
import com.reaksmey.blog.model.Blog;
import org.springframework.stereotype.Component;

@Component
public class BlogMapper {

	public BlogResponse toDto(Blog blog) {
		return new BlogResponse(
			blog.getId(),
			blog.getTitle(),
			blog.getSlug(),
			blog.getContent(),
			blog.getExcerpt(),
			blog.getAuthor(),
			blog.getFeaturedImageUrl(),
			blog.getStatus().name()
		);
	}
}
