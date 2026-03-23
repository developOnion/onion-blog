CREATE TABLE IF NOT EXISTS tokens
(
    id         serial       NOT NULL,
    token      varchar(255) NOT NULL,
    token_type varchar(255) NOT NULL,
    revoked    boolean      NOT NULL,
    expired    boolean      NOT NULL,
    user_id    uuid         NOT NULL,
    CONSTRAINT token_pkey PRIMARY KEY (id),
    CONSTRAINT token_token_key UNIQUE (token),
    CONSTRAINT fk_token_users FOREIGN KEY (user_id) REFERENCES users (id)
);
