CREATE TABLE users (
                       id             UUID PRIMARY KEY,
                       username       VARCHAR(30)  NOT NULL,
                       email          VARCHAR(150) NOT NULL,
                       password       VARCHAR(255) NOT NULL,
                       bio            VARCHAR(300),
                       avatar_url     VARCHAR(500),
                       active         BOOLEAN      NOT NULL DEFAULT TRUE,
                       created_at     TIMESTAMP    NOT NULL,
                       updated_at     TIMESTAMP    NOT NULL,

                       CONSTRAINT uk_users_username UNIQUE (username),
                       CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE movies (
                        id               UUID PRIMARY KEY,
                        title            VARCHAR(255)  NOT NULL,
                        release_year     INTEGER       NOT NULL,
                        director         VARCHAR(255)  NOT NULL,
                        synopsis         VARCHAR(1000),
                        poster_url       VARCHAR(500),
                        runtime_minutes  INTEGER,
                        active           BOOLEAN       NOT NULL DEFAULT TRUE,
                        created_at       TIMESTAMP     NOT NULL
);

CREATE TABLE movie_genres (
                              movie_id  UUID        NOT NULL,
                              genre     VARCHAR(30) NOT NULL,

                              CONSTRAINT fk_movie_genres_movie FOREIGN KEY (movie_id)
                                  REFERENCES movies (id) ON DELETE CASCADE
);

CREATE TABLE reviews (
                         id                 UUID PRIMARY KEY,
                         user_id            UUID          NOT NULL,
                         movie_id           UUID          NOT NULL,
                         rating             NUMERIC(2,1)  NOT NULL,
                         review_text        VARCHAR(2000),
                         contains_spoilers  BOOLEAN       NOT NULL DEFAULT FALSE,
                         created_at         TIMESTAMP     NOT NULL,
                         updated_at         TIMESTAMP     NOT NULL,

                         CONSTRAINT fk_reviews_user FOREIGN KEY (user_id)
                             REFERENCES users (id) ON DELETE CASCADE,
                         CONSTRAINT fk_reviews_movie FOREIGN KEY (movie_id)
                             REFERENCES movies (id) ON DELETE CASCADE,
                         CONSTRAINT uk_reviews_user_movie UNIQUE (user_id, movie_id),
                         CONSTRAINT ck_reviews_rating CHECK (rating BETWEEN 0.5 AND 5.0)
);

CREATE TABLE custom_lists (
                              id           UUID PRIMARY KEY,
                              user_id      UUID         NOT NULL,
                              name         VARCHAR(255) NOT NULL,
                              description  VARCHAR(500),
                              visibility   VARCHAR(20)  NOT NULL,
                              ranked       BOOLEAN      NOT NULL DEFAULT FALSE,
                              created_at   TIMESTAMP    NOT NULL,
                              updated_at   TIMESTAMP    NOT NULL,

                              CONSTRAINT fk_custom_lists_user FOREIGN KEY (user_id)
                                  REFERENCES users (id) ON DELETE CASCADE,
                              CONSTRAINT ck_custom_lists_visibility CHECK (visibility IN ('PUBLIC', 'PRIVATE'))
);

CREATE TABLE list_items (
                            id              UUID PRIMARY KEY,
                            custom_list_id  UUID      NOT NULL,
                            movie_id        UUID      NOT NULL,
                            position        INTEGER,
                            added_at        TIMESTAMP NOT NULL,

                            CONSTRAINT fk_list_items_custom_list FOREIGN KEY (custom_list_id)
                                REFERENCES custom_lists (id) ON DELETE CASCADE,
                            CONSTRAINT fk_list_items_movie FOREIGN KEY (movie_id)
                                REFERENCES movies (id) ON DELETE CASCADE,
                            CONSTRAINT uk_list_items_list_movie UNIQUE (custom_list_id, movie_id)
);

CREATE TABLE watch_lists (
                             id         UUID PRIMARY KEY,
                             user_id    UUID      NOT NULL,
                             movie_id   UUID      NOT NULL,
                             added_at   TIMESTAMP NOT NULL,

                             CONSTRAINT fk_watch_lists_user FOREIGN KEY (user_id)
                                 REFERENCES users (id) ON DELETE CASCADE,
                             CONSTRAINT fk_watch_lists_movie FOREIGN KEY (movie_id)
                                 REFERENCES movies (id) ON DELETE CASCADE,
                             CONSTRAINT uk_watch_lists_user_movie UNIQUE (user_id, movie_id)
);

CREATE TABLE follows (
                         id            UUID PRIMARY KEY,
                         follower_id   UUID      NOT NULL,
                         following_id  UUID      NOT NULL,
                         created_at    TIMESTAMP NOT NULL,

                         CONSTRAINT fk_follows_follower FOREIGN KEY (follower_id)
                             REFERENCES users (id) ON DELETE CASCADE,
                         CONSTRAINT fk_follows_following FOREIGN KEY (following_id)
                             REFERENCES users (id) ON DELETE CASCADE,
                         CONSTRAINT uk_follows_follower_following UNIQUE (follower_id, following_id),
                         CONSTRAINT ck_follows_no_self_follow CHECK (follower_id <> following_id)
);

CREATE TABLE comments (
                          id          UUID PRIMARY KEY,
                          review_id   UUID         NOT NULL,
                          user_id     UUID         NOT NULL,
                          text        VARCHAR(500) NOT NULL,
                          created_at  TIMESTAMP    NOT NULL,

                          CONSTRAINT fk_comments_review FOREIGN KEY (review_id)
                              REFERENCES reviews (id) ON DELETE CASCADE,
                          CONSTRAINT fk_comments_user FOREIGN KEY (user_id)
                              REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE likes (
                       id          UUID PRIMARY KEY,
                       review_id   UUID      NOT NULL,
                       user_id     UUID      NOT NULL,
                       created_at  TIMESTAMP NOT NULL,

                       CONSTRAINT fk_likes_review FOREIGN KEY (review_id)
                           REFERENCES reviews (id) ON DELETE CASCADE,
                       CONSTRAINT fk_likes_user FOREIGN KEY (user_id)
                           REFERENCES users (id) ON DELETE CASCADE,
                       CONSTRAINT uk_likes_user_review UNIQUE (user_id, review_id)
);