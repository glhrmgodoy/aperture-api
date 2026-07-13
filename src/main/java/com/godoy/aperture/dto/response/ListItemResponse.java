package com.godoy.aperture.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ListItemResponse(
        UUID id,
        UUID movieId,
        String movieTitle,
        Integer position,
        LocalDateTime addedAt
) {
}
