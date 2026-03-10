package com.reaksmey.blog.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "blogs")
public class Blog extends BaseEntity {

	@NotEmpty
	@Column(nullable = false)
	private String title;

	@NotEmpty
	@Column(nullable = false, unique = true)
	private String slug;

	@Lob
	@NotEmpty
	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@NotEmpty
	@Column(length = 500)
	private String excerpt;

	@NotBlank
	@Column(nullable = false)
	private String author;

	private String featuredImageUrl;

	@NotNull
	@Column(nullable = false)
	private BlogStatus status;
}
