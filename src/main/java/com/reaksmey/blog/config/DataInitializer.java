package com.reaksmey.blog.config;

import com.reaksmey.blog.model.Blog;
import com.reaksmey.blog.model.BlogStatus;
import com.reaksmey.blog.model.User;
import com.reaksmey.blog.repository.BlogRepository;
import com.reaksmey.blog.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

	private final UserRepository userRepository;
	private final BlogRepository blogRepository;
	private final PasswordEncoder passwordEncoder;

	public DataInitializer(
		UserRepository userRepository,
		BlogRepository blogRepository,
		PasswordEncoder passwordEncoder
	) {

		this.userRepository = userRepository;
		this.blogRepository = blogRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(String... args) throws Exception {

		if (userRepository.findByUsername("admin").isEmpty()) {

			User admin = new User(
				"admin",
				passwordEncoder.encode("admin123")
			);

			userRepository.save(admin);
		}

		Blog blog1 = new Blog(
			"First Blog Post",
			"first-blog-post",
			"This is the content of the first blog post.",
			"This is the excerpt of the first blog post.",
			"Admin",
			"",
			BlogStatus.PUBLISHED
		);

		Blog blog2 = new Blog(
			"Second Blog Post",
			"second-blog-post",
			"This is the content of the second blog post.",
			"This is the excerpt of the second blog post.",
			"Admin",
			"",
			BlogStatus.DRAFT
		);

		Blog blog3 = new Blog(
			"Third Blog Post",
			"third-blog-post",
			"This is the content of the third blog post.",
			"This is the excerpt of the third blog post.",
			"Admin",
			"",
			BlogStatus.PUBLISHED
		);

		blogRepository.save(blog1);
		blogRepository.save(blog2);
		blogRepository.save(blog3);
	}
}
