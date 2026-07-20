package com.godoy.aperture.controller;

import com.godoy.aperture.dto.response.WatchListResponse;
import com.godoy.aperture.service.WatchListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/{watchList}")
@RequiredArgsConstructor
public class WatchListController {

    private final WatchListService watchListService;

    @PostMapping("/{userId}/movies/{movieId}")
    public ResponseEntity<WatchListResponse> addMovie(@PathVariable UUID userId,
                                                      @PathVariable UUID movieId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(watchListService.addMovie(userId, movieId));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<WatchListResponse>> findByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(watchListService.findByUserId(userId));
    }

    @DeleteMapping("/{userId}/movies/{movieId}")
    public ResponseEntity<Void> removeMovie(@PathVariable UUID userId,
                                            @PathVariable UUID movieId) {
        watchListService.removeMovie(userId, movieId);
        return ResponseEntity.noContent().build();
    }
}
