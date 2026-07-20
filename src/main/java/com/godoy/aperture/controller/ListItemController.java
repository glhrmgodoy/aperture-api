package com.godoy.aperture.controller;

import com.godoy.aperture.dto.request.ListItemRequest;
import com.godoy.aperture.dto.response.ListItemResponse;
import com.godoy.aperture.service.ListItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lists/{customListsId}/movies")
@RequiredArgsConstructor
public class ListItemController {

    private final ListItemService listItemService;

    @PostMapping
    public ResponseEntity<ListItemResponse> addMovie(@PathVariable UUID customListId,
                                                     @PathVariable UUID authenticatedUserId,
                                                     @RequestBody @Valid ListItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(listItemService.addMovie(customListId, authenticatedUserId, request));
    }

    @GetMapping
    public ResponseEntity<List<ListItemResponse>> findByList(@PathVariable UUID customListId) {
        return ResponseEntity.ok(listItemService.findByList(customListId));
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<Void> removeMovie(@PathVariable UUID customListId,
                                            @PathVariable UUID movieId,
                                            @PathVariable UUID authenticatedUserId) {
        listItemService.removeMovie(customListId, movieId, authenticatedUserId);
        return ResponseEntity.noContent().build();
    }
}
