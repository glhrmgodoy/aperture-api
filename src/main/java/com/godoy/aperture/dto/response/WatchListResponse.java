package com.godoy.aperture.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record WatchListResponse(
        UUID id,
        UUID movieId,
        String movieTitle,
        LocalDateTime addedAt
) {
}
