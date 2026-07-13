package com.godoy.aperture.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID reviewId,
        UUID userID,
        String username,
        String text,
        LocalDateTime createdAt
) {
}
