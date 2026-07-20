package com.godoy.aperture.controller;

import com.godoy.aperture.dto.request.CustomListRequest;
import com.godoy.aperture.dto.response.CustomListResponse;
import com.godoy.aperture.service.CustomListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lists")
@RequiredArgsConstructor
public class CustomListController {

    private final CustomListService customListService;

    @PostMapping
    public ResponseEntity<CustomListResponse> create(@RequestBody @Valid CustomListRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customListService.create(request));
    }

    @GetMapping
    public ResponseEntity<Page<CustomListResponse>> findPublicLists(Pageable pageable) {
        return ResponseEntity.ok(customListService.findPublicLists(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomListResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(customListService.findById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<CustomListResponse>> findByUserId(@PathVariable UUID userId, Pageable pageable) {
        return ResponseEntity.ok(customListService.findByUserId(userId, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomListResponse> update(@PathVariable UUID id,
                                                     @RequestParam UUID authenticatedUserId,
                                                     @RequestBody @Valid CustomListRequest request) {
        return ResponseEntity.ok(customListService.update(id, authenticatedUserId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       @RequestParam UUID authenticatedUserId) {
        customListService.delete(id, authenticatedUserId);
        return ResponseEntity.noContent().build();
    }
}
