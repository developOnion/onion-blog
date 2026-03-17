package com.reaksmey.blog.config;

import com.reaksmey.blog.model.User;
import com.reaksmey.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Profile("dev")
@Component
public class DataInitializer implements CommandLineRunner {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${app.seed.admin.username}")
	private String seedUsername;

	@Value("${app.seed.admin.password}")
	private String seedPassword;

	public DataInitializer(
		UserRepository userRepository,
		PasswordEncoder passwordEncoder
	) {

		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(String... args) throws Exception {

		if (seedPassword == null || seedPassword.isEmpty()) {
			return;
		}

		if (userRepository.findByUsername(seedUsername).isEmpty()) {
			userRepository.save(new User(
				seedUsername,
				passwordEncoder.encode(seedPassword)
			));
		}
	}
}
