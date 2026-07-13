package com.godoy.aperture.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID userId,
        String username,
        UUID movieId,
        String movieTitle,
        BigDecimal rating,
        String reviewText,
        Boolean containsSpoilers,
        LocalDateTime createdAt,
        LocalDateTime updateAt
) {
}
