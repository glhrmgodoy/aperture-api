package com.godoy.aperture.controller;

import com.godoy.aperture.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews/{reviewId}/like")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping
    public ResponseEntity<Void> toggle(@PathVariable UUID userId,
                                       @PathVariable UUID reviewId) {
        likeService.toggle(reviewId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Long> count(@PathVariable UUID reviewId) {
        return ResponseEntity.ok(likeService.countByReviewId(reviewId));
    }
}
