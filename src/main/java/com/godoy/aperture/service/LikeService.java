package com.godoy.aperture.service;

import com.godoy.aperture.domain.entity.Like;
import com.godoy.aperture.domain.entity.Review;
import com.godoy.aperture.domain.entity.User;
import com.godoy.aperture.exception.NotFoundException;
import com.godoy.aperture.repository.LikeRepository;
import com.godoy.aperture.repository.ReviewRepository;
import com.godoy.aperture.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    public void toggle(UUID reviewId, UUID userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review não encontrada"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        likeRepository.findByUserIdAndReviewId(userId, reviewId)
                .ifPresentOrElse(
                        likeRepository::delete,
                        () -> {
                            Like like = new Like();
                            like.setUser(user);
                            like.setReview(review);
                            likeRepository.save(like);
                        }
                );
    }

    public long countByReviewId(UUID reviewId) {
        reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review não encontrada"));

        return likeRepository.countByReviewId(reviewId);
    }
}
