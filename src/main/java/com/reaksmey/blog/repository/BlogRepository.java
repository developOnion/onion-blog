package com.reaksmey.blog.repository;

import com.reaksmey.blog.model.Blog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlogRepository extends JpaRepository<Blog, UUID> {
	boolean existsBySlug(String slug);

	Optional<Blog> findByIdAndAuthor_Id(UUID id, UUID authorId);
}
