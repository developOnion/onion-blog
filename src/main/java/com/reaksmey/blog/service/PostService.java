package com.reaksmey.blog.service;

import com.reaksmey.blog.dto.BlogResponse;
import com.reaksmey.blog.mapper.BlogMapper;
import com.reaksmey.blog.model.Blog;
import com.reaksmey.blog.repository.BlogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PostService {

	private final BlogRepository blogRepository;
	private final BlogMapper blogMapper;

	public PostService(
		BlogRepository blogRepository,
		BlogMapper blogMapper
	) {

		this.blogRepository = blogRepository;
		this.blogMapper = blogMapper;
	}

	public Page<BlogResponse> getAllPosts(Pageable pageable) {

		log.info("Fetching all blog posts with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
		Page<Blog> blogs = blogRepository.findAll(pageable);
		return blogs.map(blogMapper::toDto);
	}
}
