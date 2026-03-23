CREATE TABLE IF NOT EXISTS users
(
    created_at timestamp(6) NOT NULL,
    updated_at timestamp(6) NULL,
    "version"  int8         NULL,
    id         uuid         NOT NULL,
    username   varchar(30)  NOT NULL,
    "password" varchar(128) NOT NULL,
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT users_username_key UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS blogs
(
    created_at
                       timestamp(6) NOT NULL,
    updated_at         timestamp(6) NULL,
    "version"          int8         NULL,
    author_id          uuid         NOT NULL,
    id                 uuid         NOT NULL,
    excerpt            varchar(500) NOT NULL,
    "content"          text         NOT NULL,
    featured_image_url varchar(255) NULL,
    slug               varchar(255) NOT NULL,
    status             varchar(255) NOT NULL,
    title              varchar(255) NOT NULL,
    CONSTRAINT blogs_pkey PRIMARY KEY
        (
         id
            ),
    CONSTRAINT blogs_slug_key UNIQUE
        (
         slug
            ),
    CONSTRAINT blogs_status_check CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);

ALTER TABLE blogs
    ADD CONSTRAINT fk_blogs_users FOREIGN KEY (author_id) REFERENCES users (id);

CREATE TABLE IF NOT EXISTS tokens
(
    id         uuid         NOT NULL,
    token      varchar(255) NOT NULL,
    token_type varchar(255) NOT NULL,
    revoked    boolean      NOT NULL,
    expired    boolean      NOT NULL,
    user_id    uuid         NOT NULL,
    CONSTRAINT token_pkey PRIMARY KEY (id),
    CONSTRAINT token_token_key UNIQUE (token),
    CONSTRAINT fk_token_users FOREIGN KEY (user_id) REFERENCES users (id)
);
