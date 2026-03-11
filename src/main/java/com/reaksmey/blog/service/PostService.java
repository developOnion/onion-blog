package com.reaksmey.blog.service;

import com.reaksmey.blog.dto.BlogPatchRequest;
import com.reaksmey.blog.dto.BlogRequest;
import com.reaksmey.blog.dto.BlogResponse;
import com.reaksmey.blog.exception.ResourceNotFoundException;
import com.reaksmey.blog.mapper.BlogMapper;
import com.reaksmey.blog.model.Blog;
import com.reaksmey.blog.model.User;
import com.reaksmey.blog.repository.BlogRepository;
import com.reaksmey.blog.util.SlugUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class PostService {

	private final BlogRepository blogRepository;
	private final BlogMapper blogMapper;
	private final SlugUtil slugUtil;
	private static final int EXCERPT_LENGTH = 100;

	public PostService(
		BlogRepository blogRepository,
		BlogMapper blogMapper,
		SlugUtil slugUtil
	) {

		this.blogRepository = blogRepository;
		this.blogMapper = blogMapper;
		this.slugUtil = slugUtil;
	}

	public Page<BlogResponse> getAllPosts(final Pageable pageable) {

		log.info("Fetching all blog posts with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
		final Page<Blog> blogs = blogRepository.findAll(pageable);
		return blogs.map(blogMapper::toDto);
	}

	@Transactional
	public BlogResponse createPost(final BlogRequest blogRequest, final User user) {

		log.info("Creating new blog post with title: {}", blogRequest.title());

		final String slug = slugUtil.generateSlug(blogRequest.title());

		final String excerpt = blogRequest.excerpt() != null
			? blogRequest.excerpt()
			: blogRequest.content().substring(0, Math.min(EXCERPT_LENGTH, blogRequest.content().length()));

		final Blog blog = Blog.builder()
			.title(blogRequest.title())
			.slug(slug)
			.content(blogRequest.content())
			.excerpt(excerpt)
			.author(user)
			.featuredImageUrl(blogRequest.featuredImageUrl())
			.status(blogRequest.status())
			.build();

		final Blog savedBlog = blogRepository.save(blog);
		log.info("Blog post saved with id: {}", savedBlog.getId());

		return blogMapper.toDto(savedBlog);
	}

	@Transactional(readOnly = true)
	public BlogResponse getPostById(final UUID id) {

		log.info("Fetching blog post with id: {}", id);
		final Blog blog = blogRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Blog post not found with id: " + id));
		log.info("Blog post found: {}, {}", blog.getTitle(), blog.getSlug());

		return blogMapper.toDto(blog);
	}

	@Transactional
	public BlogResponse updatePost(final UUID id, final BlogPatchRequest blogRequest, final User user) {

		log.info("Updating blog post with id: {}", id);

		final Blog blog = blogRepository.findByIdAndAuthor_Id(id, user.getId())
			.orElseThrow(() -> new ResourceNotFoundException("Blog post not found with id: " + id));

		if (blogRequest.title() != null && !blogRequest.title().isBlank()) {
			blog.setTitle(blogRequest.title());
			final String slug = slugUtil.generateSlug(blogRequest.title());
			blog.setSlug(slug);
		}

		if (blogRequest.content() != null && !blogRequest.content().isBlank()) {
			blog.setContent(blogRequest.content());
		}

		if (blogRequest.featuredImageUrl() != null) {
			blog.setFeaturedImageUrl(blogRequest.featuredImageUrl());
		}

		if (blogRequest.status() != null) {
			blog.setStatus(blogRequest.status());
		}

		final Blog updatedBlog = blogRepository.save(blog);
		log.info("Blog post updated with id: {}", updatedBlog.getId());

		return blogMapper.toDto(updatedBlog);
	}
}
