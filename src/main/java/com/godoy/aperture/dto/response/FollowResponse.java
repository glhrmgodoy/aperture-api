package com.godoy.aperture.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record FollowResponse(
        UUID id,
        UUID followerId,
        String followerUsername,
        UUID followingId,
        String followingUsername,
        LocalDateTime createdAt

) {
}
