package com.godoy.aperture.service;

import com.godoy.aperture.domain.entity.Comment;
import com.godoy.aperture.domain.entity.Review;
import com.godoy.aperture.domain.entity.User;
import com.godoy.aperture.dto.request.CommentRequest;
import com.godoy.aperture.dto.response.CommentResponse;
import com.godoy.aperture.exception.BusinessException;
import com.godoy.aperture.exception.NotFoundException;
import com.godoy.aperture.mapper.CommentMapper;
import com.godoy.aperture.repository.CommentRepository;
import com.godoy.aperture.repository.ReviewRepository;
import com.godoy.aperture.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    public CommentResponse create(UUID reviewId, UUID authenticatedUserId, CommentRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review não encontrada"));

        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        Comment comment = commentMapper.toEntity(request);
        comment.setReview(review);
        comment.setUser(user);

        Comment saved = commentRepository.save(comment);

        return commentMapper.toResponse(saved);
    }

    public List<CommentResponse> findByReviewId(UUID reviewId) {
        reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review não encontrada"));

        return commentRepository.findByReviewId(reviewId)
                .stream()
                .map(commentMapper::toResponse)
                .toList();
    }

    public void delete(UUID commentId, UUID authenticatedUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->  new NotFoundException("Comentário não encontrado"));

        boolean isCommentAuthor = comment.getUser().getId().equals(authenticatedUserId);
        boolean isReviewAuthor = comment.getReview().getUser().getId().equals(authenticatedUserId);

        if (!isCommentAuthor && !isReviewAuthor) {
            throw new BusinessException("Usuário não tem permissão para remover esse comentário");
        }

        commentRepository.delete(comment);
    }
}
