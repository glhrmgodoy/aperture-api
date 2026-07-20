package com.godoy.aperture.service;

import com.godoy.aperture.domain.entity.Movie;
import com.godoy.aperture.domain.entity.Review;
import com.godoy.aperture.domain.entity.User;
import com.godoy.aperture.dto.request.ReviewRequest;
import com.godoy.aperture.dto.response.ReviewResponse;
import com.godoy.aperture.exception.BusinessException;
import com.godoy.aperture.exception.NotFoundException;
import com.godoy.aperture.mapper.ReviewMapper;
import com.godoy.aperture.repository.MovieRepository;
import com.godoy.aperture.repository.ReviewRepository;
import com.godoy.aperture.repository.UserRepository;
import com.godoy.aperture.repository.WatchListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final WatchListRepository watchListRepository;
    private final ReviewMapper reviewMapper;

    public ReviewResponse create(ReviewRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        Movie movie = movieRepository.findById(request.movieId())
                .orElseThrow(() -> new NotFoundException("Filme não encontrado"));

        if (reviewRepository.existsByUserIdAndMovieId(user.getId(), movie.getId())) {
            throw new BusinessException("Usuário já avaliou este filme");
        }

        Review review = reviewMapper.toEntity(request);

        review.setUser(user);
        review.setMovie(movie);

        Review saved = reviewRepository.save(review);

        watchListRepository.findByUserIdAndMovieId(user.getId(), movie.getId())
                .ifPresent(watchListRepository::delete);

        return reviewMapper.toResponse(saved);
    }

    public Page<ReviewResponse> findByMovieId(UUID movieId, Pageable pageable) {
        movieRepository.findById(movieId)
                .orElseThrow(() -> new NotFoundException("Filme não encontrado"));

        return reviewRepository.findByMovieId(movieId, pageable)
                .map(reviewMapper::toResponse);
    }

    public Page<ReviewResponse> findByUserId(UUID userId, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        return reviewRepository.findByUserId(userId, pageable)
                .map(reviewMapper::toResponse);
    }

    public ReviewResponse findById(UUID id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Review não encontrada"));

        return reviewMapper.toResponse(review);
    }

    public ReviewResponse update(UUID id, ReviewRequest request) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Review não encontrada"));

        review.setRating(request.rating());
        review.setReviewText(request.reviewText());
        review.setContainsSpoilers(request.containsSpoilers());

        Review updated = reviewRepository.save(review);

        return reviewMapper.toResponse(updated);
    }

    public void delete(UUID id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Review não encontrada"));

        reviewRepository.delete(review);
    }
}
