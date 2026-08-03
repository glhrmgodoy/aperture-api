package com.godoy.aperture.service;

import com.godoy.aperture.domain.entity.Like;
import com.godoy.aperture.domain.entity.Movie;
import com.godoy.aperture.domain.entity.Review;
import com.godoy.aperture.domain.entity.User;
import com.godoy.aperture.domain.enums.Genre;
import com.godoy.aperture.exception.NotFoundException;
import com.godoy.aperture.repository.LikeRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private LikeService likeService;

    private User user;
    private Movie movie;
    private Review review;
    private Like like;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .username("maria")
                .email("maria@email.com")
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
                .user(user)
                .movie(movie)
                .rating(new BigDecimal("4.5"))
                .build();

        like = Like.builder()
                .id(UUID.randomUUID())
                .user(user)
                .review(review)
                .build();
    }

    @Nested
    @DisplayName("toggle")
    class Toggle {

        @Test
        @DisplayName("Deve curtir review quando ainda não existe like do usuário")
        void shouldLikeReviewWhenUserHasNotLikedYet() {
            when(userRepository.findById(user.getId()))
                    .thenReturn(Optional.of(user));

            when(reviewRepository.findById(review.getId()))
                    .thenReturn(Optional.of(review));

            when(likeRepository.findByUserIdAndReviewId(user.getId(), review.getId()))
                    .thenReturn(Optional.empty());

            likeService.toggle(review.getId(), user.getId());

            ArgumentCaptor<Like> captor = ArgumentCaptor.forClass(Like.class);

            verify(likeRepository).save(captor.capture());

            Like captured = captor.getValue();

            assertThat(captured.getUser()).isEqualTo(user);
            assertThat(captured.getReview()).isEqualTo(review);

            verify(userRepository).findById(user.getId());
            verify(reviewRepository).findById(review.getId());
            verify(likeRepository).findByUserIdAndReviewId(user.getId(), review.getId());
            verify(likeRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Deve descurtir a review quando já existe like do usuário")
        void shouldUnlikeReviewWhenUserHasAlreadyLiked() {
            when(userRepository.findById(user.getId()))
                    .thenReturn(Optional.of(user));

            when(reviewRepository.findById(review.getId()))
                    .thenReturn(Optional.of(review));

            when(likeRepository.findByUserIdAndReviewId(user.getId(), review.getId()))
                    .thenReturn(Optional.of(like));

            likeService.toggle(review.getId(), user.getId());

            verify(userRepository).findById(user.getId());
            verify(reviewRepository).findById(review.getId());
            verify(likeRepository).findByUserIdAndReviewId(user.getId(), review.getId());

            verify(likeRepository).delete(like);
            verify(likeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao curtir review não inexistente")
        void shouldThrowNotFoundExceptionWhenReviewDoesNotExist() {
            UUID nonExistentReviewId = UUID.randomUUID();

            when(reviewRepository.findById(nonExistentReviewId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    likeService.toggle(nonExistentReviewId, user.getId()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Review não encontrada");

            verify(reviewRepository).findById(nonExistentReviewId);

            verifyNoInteractions(userRepository, likeRepository);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando usuário não existe")
        void shouldThrowNotFoundExceptionWhenUserDoesNotExist() {
            UUID nonExistentUserId = UUID.randomUUID();

            when(reviewRepository.findById(review.getId()))
                    .thenReturn(Optional.of(review));

            when(userRepository.findById(nonExistentUserId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    likeService.toggle(review.getId(), nonExistentUserId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Usuário não encontrado");

            verify(reviewRepository).findById(review.getId());
            verify(userRepository).findById(nonExistentUserId);

            verifyNoInteractions(likeRepository);
        }
    }

    @Nested
    @DisplayName("countByReviewId")
    class CountByReviewId {

        @Test
        @DisplayName("Deve retorna a quantidade de curtidas de review com sucesso")
        void shouldReturnReviewLikeCountSuccessfully() {
            when(reviewRepository.findById(review.getId()))
                    .thenReturn(Optional.of(review));

            when(likeRepository.countByReviewId(review.getId()))
                    .thenReturn(3L);

            long result = likeService.countByReviewId(review.getId());

            assertThat(result).isEqualTo(3L);

            verify(reviewRepository).findById(review.getId());
            verify(likeRepository).countByReviewId(review.getId());
        }

        @Test
        @DisplayName("Deve retornar zero quando review não tem curtidas")
        void shouldReturnZeroWhenReviewHasNoLikes() {
            when(reviewRepository.findById(review.getId()))
                    .thenReturn(Optional.of(review));

            when(likeRepository.countByReviewId(review.getId()))
                    .thenReturn(0L);

            long result = likeService.countByReviewId(review.getId());

            assertThat(result).isZero();

            verify(reviewRepository).findById(review.getId());
            verify(likeRepository).countByReviewId(review.getId());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao contar curtidas de review inexistente")
        void shouldThrowNotFoundExceptionWhenCountingLikesOfNonExistentReview() {
            UUID nonExistentReviewId = UUID.randomUUID();

            when(reviewRepository.findById(nonExistentReviewId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    likeService.countByReviewId(nonExistentReviewId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Review não encontrada");

            verify(reviewRepository).findById(nonExistentReviewId);

            verifyNoInteractions(likeRepository);
        }
    }
}