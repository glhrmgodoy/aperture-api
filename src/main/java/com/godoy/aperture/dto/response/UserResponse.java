package com.godoy.aperture.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String bio,
        String avatarUrl,
        LocalDateTime createdAt
) {
}
