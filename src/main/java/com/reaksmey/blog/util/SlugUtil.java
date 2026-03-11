package com.reaksmey.blog.util;

import com.github.slugify.Slugify;
import com.reaksmey.blog.repository.BlogRepository;
import org.springframework.stereotype.Component;

@Component
public class SlugUtil {

	private final Slugify slugify;
	private final BlogRepository blogRepository;

	public SlugUtil(
		Slugify slugify,
		BlogRepository blogRepository
	) {

		this.slugify = slugify;
		this.blogRepository = blogRepository;
	}

	public String generateSlug(final String title) {

		String slug = slugify.slugify(title);
		if (blogRepository.existsBySlug(slug)) {
			slug += "-" + System.currentTimeMillis();
		}
		return slug;
	}
}
