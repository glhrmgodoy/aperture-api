package com.godoy.aperture.controller;

import com.godoy.aperture.dto.request.CommentRequest;
import com.godoy.aperture.dto.response.CommentResponse;
import com.godoy.aperture.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews/{reviewId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> create(@PathVariable UUID reviewId,
                                                  @PathVariable UUID authenticatedUserId,
                                                  @RequestBody @Valid CommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.create(reviewId, authenticatedUserId, request));
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> findByReviewId(@PathVariable UUID reviewId) {
        return ResponseEntity.ok(commentService.findByReviewId(reviewId));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(@PathVariable UUID commentId,
                                       @PathVariable UUID authenticatedUserId) {
        commentService.delete(commentId, authenticatedUserId);
        return ResponseEntity.noContent().build();
    }
}
