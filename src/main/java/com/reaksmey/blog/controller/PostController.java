package com.reaksmey.blog.controller;

import com.reaksmey.blog.dto.BlogResponse;
import com.reaksmey.blog.service.PostService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
@Tag(name = "PostController", description = "Endpoints for managing blog posts")
public class PostController {

	private final PostService postService;

	public PostController(PostService postService) {
		this.postService = postService;
	}

	@GetMapping
	public ResponseEntity<Page<BlogResponse>> getAllPosts(
		@PageableDefault(
			sort = "createdAt",
			direction = Sort.Direction.DESC
		)
		Pageable pageable
	) {

		Page<BlogResponse> posts = postService.getAllPosts(pageable);
		return ResponseEntity.ok().body(posts);
	}
}
