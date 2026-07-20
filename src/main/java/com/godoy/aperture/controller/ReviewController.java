package com.godoy.aperture.controller;

import com.godoy.aperture.dto.request.ReviewRequest;
import com.godoy.aperture.dto.response.ReviewResponse;
import com.godoy.aperture.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponse> create(@RequestBody @Valid ReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(reviewService.findById(id));
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<Page<ReviewResponse>> findByMovieId(@PathVariable UUID movieId, Pageable pageable) {
        return ResponseEntity.ok(reviewService.findByMovieId(movieId, pageable));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ReviewResponse>> findByUserId(@PathVariable UUID userId, Pageable pageable) {
        return ResponseEntity.ok(reviewService.findByUserId(userId, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponse> update(@PathVariable UUID id,
                                                 @RequestBody @Valid ReviewRequest request) {
        return ResponseEntity.ok(reviewService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
