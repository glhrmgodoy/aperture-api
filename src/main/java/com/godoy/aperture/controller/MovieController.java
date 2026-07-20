package com.godoy.aperture.controller;

import com.godoy.aperture.domain.enums.Genre;
import com.godoy.aperture.dto.request.MovieRequest;
import com.godoy.aperture.dto.response.MovieResponse;
import com.godoy.aperture.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @PostMapping
    public ResponseEntity<MovieResponse> create(@RequestBody MovieRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movieService.create(request));
    }

    @GetMapping(params = "title")
    public ResponseEntity<Page<MovieResponse>> findByTitle(@RequestParam String title, Pageable pageable) {
        return ResponseEntity.ok(movieService.findByTitle(title, pageable));
    }

    @GetMapping(params = "genre")
    public ResponseEntity<Page<MovieResponse>> findByGenre(@RequestParam Genre genre, Pageable pageable) {
        return ResponseEntity.ok(movieService.findByGenre(genre, pageable));
    }

    @GetMapping(params = "releaseYear")
    public ResponseEntity<Page<MovieResponse>> findByReleaseYear(@RequestParam Integer releaseYear, Pageable pageable) {
        return ResponseEntity.ok(movieService.findByReleaseYear(releaseYear, pageable));
    }

    @GetMapping(params = "director")
    public ResponseEntity<Page<MovieResponse>> findByDirector(@RequestParam String director, Pageable pageable) {
        return ResponseEntity.ok(movieService.findByDirector(director, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(movieService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovieResponse> update(@PathVariable UUID id,
                                                @RequestBody MovieRequest request) {
        return ResponseEntity.ok(movieService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        movieService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
