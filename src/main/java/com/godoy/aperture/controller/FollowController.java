package com.godoy.aperture.controller;

import com.godoy.aperture.dto.response.FollowResponse;
import com.godoy.aperture.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{followerId}/follow/{followingId}")
    public ResponseEntity<FollowResponse> follow(@PathVariable UUID followerId, @PathVariable UUID followingId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(followService.follow(followerId, followingId));
    }

    @DeleteMapping("/{followerId}/unfollow/{followingId}")
    public ResponseEntity<Void> unfollow(@PathVariable UUID followerId, @PathVariable UUID followingId) {
        followService.unfollow(followerId, followingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<FollowResponse>> findFollowers(@PathVariable UUID userId) {
        return ResponseEntity.ok(followService.findFollowers(userId));
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<List<FollowResponse>> findFollowing(@PathVariable UUID userId) {
        return ResponseEntity.ok(followService.findFollowing(userId));
    }
}
