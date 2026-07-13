package com.godoy.aperture.dto.response;

import com.godoy.aperture.domain.enums.Genre;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MovieResponse(
        UUID id,
        String title,
        Integer releaseYear,
        String director,
        String synopsis,
        String posterUrl,
        Integer runTimeMinutes,
        List<Genre> genres,
        LocalDateTime createdAt
) {
}
