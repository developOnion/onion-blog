CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO users (id, created_at, updated_at, "version", username, "password")
VALUES (gen_random_uuid(),
        now(),
        NULL,
        0,
        '${ADMIN_USERNAME}',
        '${ADMIN_PASSWORD_HASH}') ON CONFLICT (username) DO NOTHING;