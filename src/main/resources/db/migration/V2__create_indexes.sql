CREATE INDEX idx_reviews_movie_id ON reviews (movie_id);

CREATE INDEX idx_custom_lists_user_id ON custom_lists (user_id);
CREATE INDEX idx_custom_lists_visibility ON custom_lists (visibility);

CREATE INDEX idx_list_items_movie_id ON list_items (movie_id);

CREATE INDEX idx_watch_lists_movie_id ON watch_lists (movie_id);

CREATE INDEX idx_follows_following_id ON follows (following_id);

CREATE INDEX idx_comments_review_id ON comments (review_id);
CREATE INDEX idx_comments_user_id ON comments (user_id);

CREATE INDEX idx_likes_review_id ON likes (review_id);

CREATE INDEX idx_movie_genres_movie_id ON movie_genres (movie_id);
CREATE INDEX idx_movie_genres_genre ON movie_genres (genre);