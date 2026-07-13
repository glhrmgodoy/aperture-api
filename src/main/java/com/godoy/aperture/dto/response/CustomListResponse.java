package com.godoy.aperture.dto.response;

import com.godoy.aperture.domain.enums.ListVisibility;

import java.time.LocalDateTime;
import java.util.UUID;

public record CustomListResponse(
        UUID id,
        UUID userId,
        String name,
        String description,
        ListVisibility visibility,
        Boolean ranked,
        LocalDateTime createdAt
) {
}
