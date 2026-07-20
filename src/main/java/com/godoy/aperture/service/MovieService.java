package com.godoy.aperture.service;

import com.godoy.aperture.domain.entity.Movie;
import com.godoy.aperture.domain.enums.Genre;
import com.godoy.aperture.dto.request.MovieRequest;
import com.godoy.aperture.dto.response.MovieResponse;
import com.godoy.aperture.exception.NotFoundException;
import com.godoy.aperture.mapper.MovieMapper;
import com.godoy.aperture.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;

    public MovieResponse create(MovieRequest request) {
        Movie movie = movieMapper.toEntity(request);
        Movie saved = movieRepository.save(movie);

        return movieMapper.toResponse(saved);
    }

    public Page<MovieResponse> findByTitle(String title, Pageable pageable) {
        return movieRepository.findByTitleContainingIgnoreCase(title, pageable)
                .map(movieMapper::toResponse);
    }

    public Page<MovieResponse> findByGenre(Genre genre, Pageable pageable) {
        return movieRepository.findByGenresContaining(genre, pageable)
                .map(movieMapper::toResponse);
    }

    public Page<MovieResponse> findByReleaseYear(Integer releaseYear, Pageable pageable) {
        return movieRepository.findByReleaseYear(releaseYear, pageable)
                .map(movieMapper::toResponse);
    }

    public Page<MovieResponse> findByDirector(String director, Pageable pageable) {
        return movieRepository.findByDirectorContainingIgnoreCase(director, pageable)
                .map(movieMapper::toResponse);
    }

    public MovieResponse findById(UUID id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Filme não encontrado"));

        return movieMapper.toResponse(movie);
    }

    public MovieResponse update(UUID id, MovieRequest request) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Filme não encontrado"));

        movie.setTitle(request.title());
        movie.setReleaseYear(request.releaseYear());
        movie.setDirector(request.director());
        movie.setSynopsis(request.synopsis());
        movie.setPosterUrl(request.posterUrl());
        movie.setRuntimeMinutes(request.runtimeMinutes());
        movie.setGenres(request.genres());

        Movie updated = movieRepository.save(movie);

        return movieMapper.toResponse(updated);
    }

    public void delete(UUID id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Filme não encontrado"));

        movie.setActive(false);
        movieRepository.save(movie);
    }
}
