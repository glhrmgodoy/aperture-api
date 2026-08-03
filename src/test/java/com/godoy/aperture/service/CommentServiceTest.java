package com.godoy.aperture.service;

import com.godoy.aperture.domain.entity.Comment;
import com.godoy.aperture.domain.entity.Movie;
import com.godoy.aperture.domain.entity.Review;
import com.godoy.aperture.domain.entity.User;
import com.godoy.aperture.domain.enums.Genre;
import com.godoy.aperture.dto.request.CommentRequest;
import com.godoy.aperture.dto.response.CommentResponse;
import com.godoy.aperture.exception.BusinessException;
import com.godoy.aperture.exception.NotFoundException;
import com.godoy.aperture.mapper.CommentMapper;
import com.godoy.aperture.repository.CommentRepository;
import com.godoy.aperture.repository.ReviewRepository;
import com.godoy.aperture.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentService commentService;

    private User reviewAuthor;
    private User commenter;
    private Movie movie;
    private Review review;
    private Comment comment;
    private CommentRequest request;
    private CommentResponse response;

    @BeforeEach
    void setUp() {
        reviewAuthor = User.builder()
                .id(UUID.randomUUID())
                .username("maria")
                .email("maria@email.com")
                .password("12345")
                .active(true)
                .build();

        commenter = User.builder()
                .id(UUID.randomUUID())
                .username("ana")
                .email("ana@email.com")
                .password("12345")
                .active(true)
                .build();

        movie = Movie.builder()
                .id(UUID.randomUUID())
                .title("Lawrence of Arabia")
                .releaseYear(1962)
                .director("David Lean")
                .runtimeMinutes(228)
                .genres(List.of(Genre.ADVENTURE, Genre.WAR))
                .active(true)
                .build();

        review = Review.builder()
                .id(UUID.randomUUID())
                .user(reviewAuthor)
                .movie(movie)
                .rating(new BigDecimal("4.5"))
                .build();

        request = new CommentRequest(
                "Meu filme favorito!"
        );

        comment = Comment.builder()
                .id(UUID.randomUUID())
                .review(review)
                .user(commenter)
                .text(request.text())
                .build();

        response = new CommentResponse(
                comment.getId(),
                commenter.getId(),
                commenter.getUsername(),
                comment.getText(),
                null
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Deve criar comentário com sucesso")
        void shouldCreateCommentSuccessfully() {
            when(reviewRepository.findById(review.getId()))
                    .thenReturn(Optional.of(review));

            when(userRepository.findById(commenter.getId()))
                    .thenReturn(Optional.of(commenter));

            when(commentMapper.toEntity(request))
                    .thenReturn(comment);

            when(commentRepository.save(any(Comment.class)))
                    .thenReturn(comment);

            when(commentMapper.toResponse(comment))
                    .thenReturn(response);

            CommentResponse result = commentService.create(review.getId(), commenter.getId(), request);

            ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);

            verify(commentRepository).save(captor.capture());

            Comment captured = captor.getValue();

            assertThat(captured.getReview()).isEqualTo(review);
            assertThat(captured.getUser()).isEqualTo(commenter);
            assertThat(captured.getText()).isEqualTo(request.text());

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(comment.getId());
            assertThat(result.text()).isEqualTo(request.text());

            verify(reviewRepository).findById(review.getId());
            verify(userRepository).findById(commenter.getId());
            verify(commentMapper).toEntity(request);
            verify(commentMapper).toResponse(comment);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao comentar em review inexistente")
        void shouldThrowNotFoundExceptionWhenCommentingOnNonExistingReview() {
            UUID nonExistingReviewId = UUID.randomUUID();

            when(reviewRepository.findById(nonExistingReviewId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    commentService.create(nonExistingReviewId, commenter.getId(), request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Review não encontrada");

            verify(reviewRepository).findById(nonExistingReviewId);

            verifyNoInteractions(userRepository, commentRepository, commentMapper);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando usuário não existe")
        void shouldThrowNotFoundExceptionWhenUserNonExistent() {
            UUID nonExistingUserId = UUID.randomUUID();

            when(reviewRepository.findById(review.getId()))
                    .thenReturn(Optional.of(review));

            when(userRepository.findById(nonExistingUserId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    commentService.create(review.getId(), nonExistingUserId, request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Usuário não encontrado");

            verify(reviewRepository).findById(review.getId());
            verify(userRepository).findById(nonExistingUserId);

            verifyNoInteractions(commentRepository, commentMapper);
        }
    }

    @Nested
    @DisplayName("findByReviewId")
    class FindByReviewId {

        @Test
        @DisplayName("Deve retornar lista de comentários da review por ID com sucesso")
        void shouldReturnListOfReviewCommentsByIdSuccessfully() {
            when(reviewRepository.findById(review.getId()))
                    .thenReturn(Optional.of(review));

            when(commentRepository.findByReviewId(review.getId()))
                    .thenReturn(List.of(comment));

            when(commentMapper.toResponse(comment))
                    .thenReturn(response);

            List<CommentResponse> result = commentService.findByReviewId(review.getId());

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().id()).isEqualTo(comment.getId());
            assertThat(result.getFirst().text()).isEqualTo(comment.getText());

            verify(reviewRepository).findById(review.getId());
            verify(commentRepository).findByReviewId(review.getId());
            verify(commentMapper).toResponse(comment);
        }

        @Test
        @DisplayName("Deve retornar NotFoundException ao buscar comentários de review inexistente")
        void shouldThrowNotFoundExceptionWhenFindingCommentsOfNonExistentReview() {
            UUID nonExistingReviewId = UUID.randomUUID();

            when(reviewRepository.findById(nonExistingReviewId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    commentService.findByReviewId(nonExistingReviewId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Review não encontrada");

            verify(reviewRepository).findById(nonExistingReviewId);

            verifyNoInteractions(commentRepository, commentMapper);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando review não tem comentários")
        void shouldReturnEmptyListWhenReviewHasNoComments() {
            when(reviewRepository.findById(review.getId()))
                    .thenReturn(Optional.of(review));

            when(commentRepository.findByReviewId(review.getId()))
                    .thenReturn(List.of());

            List<CommentResponse> result = commentService.findByReviewId(review.getId());

            assertThat(result).isNotNull();
            assertThat(result).isEmpty();

            verify(reviewRepository).findById(review.getId());
            verify(commentRepository).findByReviewId(review.getId());

            verifyNoInteractions(commentMapper);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("Deve deletar comentário com sucesso quando solicitante é o autor do comentário")
        void shouldDeleteCommentSuccessfullyWhenRequesterIsCommentAuthor() {
            when(commentRepository.findById(comment.getId()))
                    .thenReturn(Optional.of(comment));

            commentService.delete(comment.getId(), commenter.getId());

            verify(commentRepository).findById(comment.getId());
            verify(commentRepository).delete(comment);
        }

        @Test
        @DisplayName("Deve deletar comentário com sucesso quando solicitante é o autor da review")
        void shouldDeleteCommentSuccessfullyWhenRequesterIsReviewAuthor() {
            when(commentRepository.findById(comment.getId()))
                    .thenReturn(Optional.of(comment));

            commentService.delete(comment.getId(), reviewAuthor.getId());

            verify(commentRepository).findById(comment.getId());
            verify(commentRepository).delete(comment);
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando solicitante não é o autor do comentário nem da review")
        void shouldThrowBusinessExceptionWhenRequesterIsNeitherCommentNorReviewAuthor() {
            UUID otherUserId = UUID.randomUUID();

            when(commentRepository.findById(comment.getId()))
                    .thenReturn(Optional.of(comment));

            assertThatThrownBy(() ->
                    commentService.delete(comment.getId(), otherUserId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Usuário não tem permissão para remover esse comentário");

            verify(commentRepository).findById(comment.getId());
            verify(commentRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao deletar comentário inexistente")
        void shouldThrowNotFoundExceptionWhenDeletingNonExistentComment() {
            UUID nonExistingCommentId = UUID.randomUUID();

            when(commentRepository.findById(nonExistingCommentId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    commentService.delete(nonExistingCommentId, commenter.getId()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Comentário não encontrado");

            verify(commentRepository).findById(nonExistingCommentId);
            verify(commentRepository, never()).delete(any());
        }
    }

}