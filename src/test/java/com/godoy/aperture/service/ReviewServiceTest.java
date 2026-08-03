package com.godoy.aperture.service;

import com.godoy.aperture.domain.entity.Movie;
import com.godoy.aperture.domain.entity.Review;
import com.godoy.aperture.domain.entity.User;
import com.godoy.aperture.domain.entity.WatchList;
import com.godoy.aperture.domain.enums.Genre;
import com.godoy.aperture.dto.request.ReviewRequest;
import com.godoy.aperture.dto.response.ReviewResponse;
import com.godoy.aperture.exception.BusinessException;
import com.godoy.aperture.exception.NotFoundException;
import com.godoy.aperture.mapper.ReviewMapper;
import com.godoy.aperture.repository.MovieRepository;
import com.godoy.aperture.repository.ReviewRepository;
import com.godoy.aperture.repository.UserRepository;
import com.godoy.aperture.repository.WatchListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private WatchListRepository watchListRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewService reviewService;

    private User user;
    private Movie movie;
    private Review review;
    private ReviewRequest request;
    private ReviewResponse response;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .username("username")
                .email("email")
                .password("password")
                .active(true)
                .build();

        movie = Movie.builder()
                .id(UUID.randomUUID())
                .title("Barry Lyndon")
                .releaseYear(1975)
                .director("Stanley Kubrick")
                .runtimeMinutes(188)
                .genres(List.of(Genre.DRAMA,Genre.WAR, Genre.HISTORY))
                .active(true)
                .build();

        request = new ReviewRequest(
                user.getId(),
                movie.getId(),
                new BigDecimal("5.0"),
                "",
                false
        );

        review = Review.builder()
                .id(UUID.randomUUID())
                .user(user)
                .movie(movie)
                .rating(request.rating())
                .reviewText(request.reviewText())
                .containsSpoilers(request.containsSpoilers())
                .build();

        response = new ReviewResponse(
                review.getId(),
                user.getId(),
                user.getUsername(),
                movie.getId(),
                movie.getTitle(),
                review.getRating(),
                review.getReviewText(),
                review.getContainsSpoilers(),
                null,
                null
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Deve criar review com sucesso")
        void shouldCreateReviewSuccessfully() {
            when(userRepository.findById(request.userId()))
                    .thenReturn(Optional.of(user));

            when(movieRepository.findById(request.movieId()))
                    .thenReturn(Optional.of(movie));

            when(reviewRepository.existsByUserIdAndMovieId(user.getId(), movie.getId()))
                    .thenReturn(false);

            when(reviewMapper.toEntity(request))
                    .thenReturn(review);

            when(reviewRepository.save(any(Review.class)))
                    .thenReturn(review);

            when(watchListRepository.findByUserIdAndMovieId(user.getId(), movie.getId()))
                    .thenReturn(Optional.empty());

            when(reviewMapper.toResponse(review))
                    .thenReturn(response);

            ReviewResponse result = reviewService.create(request);

            assertThat(result).isNotNull();
            assertThat(result.userId()).isEqualTo(request.userId());
            assertThat(result.movieId()).isEqualTo(request.movieId());
            assertThat(result.rating()).isEqualByComparingTo(request.rating());
            assertThat(result.reviewText()).isEqualTo(request.reviewText());
            assertThat(result.containsSpoilers()).isEqualTo(request.containsSpoilers());

            verify(userRepository).findById(request.userId());
            verify(movieRepository).findById(request.movieId());
            verify(reviewRepository).existsByUserIdAndMovieId(user.getId(), movie.getId());
            verify(watchListRepository).findByUserIdAndMovieId(user.getId(), movie.getId());

            verify(reviewRepository).save(review);
            verify(reviewMapper).toEntity(request);
            verify(reviewMapper).toResponse(review);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando usuário não encontrado")
        void shouldThrowNotFoundExceptionWhenUserNotFound() {
            when(userRepository.findById(request.userId()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    reviewService.create(request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Usuário não encontrado");

            verify(userRepository).findById(request.userId());

            verifyNoInteractions(reviewRepository, movieRepository, watchListRepository, reviewMapper);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando filme não encontrado")
        void shouldThrowNotFoundExceptionWhenMovieNotFound() {
            when(userRepository.findById(request.userId()))
                    .thenReturn(Optional.of(user));

            when(movieRepository.findById(request.movieId()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    reviewService.create(request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Filme não encontrado");

            verify(userRepository).findById(request.userId());
            verify(movieRepository).findById(request.movieId());

            verifyNoInteractions(reviewRepository, watchListRepository, reviewMapper);
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando usuário já avaliou filme")
        void shouldThrowBusinessExceptionWhenMovieAlreadyRatedByUser() {
            when(userRepository.findById(request.userId()))
                    .thenReturn(Optional.of(user));

            when(movieRepository.findById(request.movieId()))
                    .thenReturn(Optional.of(movie));

            when(reviewRepository.existsByUserIdAndMovieId(user.getId(), movie.getId()))
                    .thenReturn(true);

            assertThatThrownBy(() ->
                    reviewService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Usuário já avaliou este filme");

            verify(userRepository).findById(request.userId());
            verify(movieRepository).findById(request.movieId());
            verify(reviewRepository).existsByUserIdAndMovieId(user.getId(), movie.getId());
            verify(reviewRepository, never()).save(any());

            verifyNoInteractions(watchListRepository, reviewMapper);
        }

        @Test
        @DisplayName("Deve remover filme da watchlist ao criar review")
        void shouldDeleteWatchListWhenCreatingReview() {
            WatchList watchList = WatchList.builder()
                    .id(UUID.randomUUID())
                    .user(user)
                    .movie(movie)
                    .build();

            when(userRepository.findById(request.userId()))
                    .thenReturn(Optional.of(user));

            when(movieRepository.findById(request.movieId()))
                    .thenReturn(Optional.of(movie));

            when(reviewRepository.existsByUserIdAndMovieId(user.getId(), movie.getId()))
                    .thenReturn(false);

            when(reviewMapper.toEntity(request))
                    .thenReturn(review);

            when(reviewRepository.save(any(Review.class)))
                    .thenReturn(review);

            when(watchListRepository.findByUserIdAndMovieId(user.getId(), movie.getId()))
                    .thenReturn(Optional.of(watchList));

            when(reviewMapper.toResponse(review))
                    .thenReturn(response);

            reviewService.create(request);

            verify(userRepository).findById(request.userId());
            verify(movieRepository).findById(request.movieId());
            verify(reviewRepository).existsByUserIdAndMovieId(user.getId(), movie.getId());
            verify(reviewMapper).toEntity(request);
            verify(reviewRepository).save(review);
            verify(watchListRepository).findByUserIdAndMovieId(user.getId(), movie.getId());
            verify(reviewMapper).toResponse(review);
            verify(watchListRepository).delete(watchList);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Deve retornar review por ID com sucesso")
        void shouldFindReviewByIdSuccessfully() {
            when(reviewRepository.findById(review.getId()))
                    .thenReturn(Optional.of(review));

            when(reviewMapper.toResponse(review))
                    .thenReturn(response);

            ReviewResponse result = reviewService.findById(review.getId());

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(review.getId());
            assertThat(result.rating()).isEqualByComparingTo(review.getRating());
            assertThat(result.reviewText()).isEqualTo(review.getReviewText());
            assertThat(result.containsSpoilers()).isEqualTo(review.getContainsSpoilers());

            verify(reviewRepository).findById(review.getId());
            verify(reviewMapper).toResponse(review);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando review não encontrado")
        void shouldThrowNotFoundExceptionWhenReviewNotFound() {
            UUID nonExistentReviewId = UUID.randomUUID();

            when(reviewRepository.findById(nonExistentReviewId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    reviewService.findById(nonExistentReviewId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Review não encontrada");

            verify(reviewRepository).findById(nonExistentReviewId);

            verifyNoInteractions(reviewMapper);
        }
    }

    @Nested
    @DisplayName("findByMovieId")
    class FindByMovieId {

        @Test
        @DisplayName("Deve retornar página de reviews de um filme por ID com sucesso")
        void shouldReturnPageOfReviewsByMovieIdSuccessfully() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<Review> page = new PageImpl<>(
                    List.of(review),
                    pageable,
                    1
            );

            when(movieRepository.findById(movie.getId()))
                    .thenReturn(Optional.of(movie));

            when(reviewRepository.findByMovieId(movie.getId(), pageable))
                    .thenReturn(page);

            when(reviewMapper.toResponse(review))
                    .thenReturn(response);

            Page<ReviewResponse> result = reviewService.findByMovieId(movie.getId(), pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().id()).isEqualTo(review.getId());
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getNumber()).isZero();

            verify(movieRepository).findById(movie.getId());
            verify(reviewRepository).findByMovieId(movie.getId(), pageable);
            verify(reviewMapper).toResponse(review);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando filme não encontrado")
        void shouldThrowNotFoundExceptionWhenMovieNotFound() {
            UUID nonExistentMovieId = UUID.randomUUID();

            Pageable pageable = PageRequest.of(0, 10);

            when(movieRepository.findById(nonExistentMovieId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    reviewService.findByMovieId(nonExistentMovieId, pageable))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Filme não encontrado");

           verify(movieRepository).findById(nonExistentMovieId);

           verifyNoInteractions(reviewRepository, reviewMapper);
        }

        @Test
        @DisplayName("Deve retornar página vazia quando filme não possuir reviews")
        void shouldReturnEmptyPageWhenMovieHasNoReviews() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<Review> emptyPage = Page.empty(pageable);

            when(movieRepository.findById(movie.getId()))
                    .thenReturn(Optional.of(movie));

            when(reviewRepository.findByMovieId(movie.getId(), pageable))
                    .thenReturn(emptyPage);

            Page<ReviewResponse> result = reviewService.findByMovieId(movie.getId(), pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getTotalPages()).isZero();

            verify(movieRepository).findById(movie.getId());
            verify(reviewRepository).findByMovieId(movie.getId(), pageable);

            verifyNoInteractions(reviewMapper);
        }
    }

    @Nested
    @DisplayName("findByUserId")
    class FindByUserId {

        @Test
        @DisplayName("Deve retornar página de avalições de um usuário por ID com sucesso")
        void shouldReturnPageOfReviewsByUserIdSuccessfully() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<Review> page = new PageImpl<>(
                    List.of(review),
                    pageable,
                    1
            );

            when(userRepository.findById(user.getId()))
                    .thenReturn(Optional.of(user));

            when(reviewRepository.findByUserId(user.getId(), pageable))
                    .thenReturn(page);

            when(reviewMapper.toResponse(review))
                    .thenReturn(response);

            Page<ReviewResponse> result = reviewService.findByUserId(user.getId(), pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().id()).isEqualTo(review.getId());
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getNumber()).isZero();

            verify(userRepository).findById(user.getId());
            verify(reviewRepository).findByUserId(user.getId(), pageable);
            verify(reviewMapper).toResponse(review);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando usuário não encontrado")
        void shouldThrowNotFoundExceptionWhenUserNotFound() {
            UUID nonExistentUserId = UUID.randomUUID();

            Pageable pageable = PageRequest.of(0, 10);

            when(userRepository.findById(nonExistentUserId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.findByUserId(nonExistentUserId, pageable))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Usuário não encontrado");

            verify(userRepository).findById(nonExistentUserId);

            verifyNoInteractions(reviewMapper);
        }

        @Test
        @DisplayName("Deve retornar página vazia quando usuário não possuir reviews")
        void shouldReturnEmptyPageWhenUserHasNoReviews() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<Review> emptyPage = Page.empty(pageable);

            when(userRepository.findById(user.getId()))
                    .thenReturn(Optional.of(user));

            when(reviewRepository.findByUserId(user.getId(), pageable))
                    .thenReturn(emptyPage);

            Page<ReviewResponse> result = reviewService.findByUserId(user.getId(), pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getTotalPages()).isZero();

            verify(userRepository).findById(user.getId());
            verify(reviewRepository).findByUserId(user.getId(), pageable);

            verifyNoInteractions(reviewMapper);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("Deve atualizar review com sucesso")
        void shouldUpdateReviewSuccessfully() {
            ReviewRequest updateRequest = new ReviewRequest(
                    user.getId(),
                    movie.getId(),
                    new BigDecimal("4.0"),
                    "Revisando minha nota depois de assistir ao filme novamente.",
                    true
            );

            ReviewResponse updateResponse = new ReviewResponse(
                    review.getId(),
                    user.getId(),
                    user.getUsername(),
                    movie.getId(),
                    movie.getTitle(),
                    updateRequest.rating(),
                    updateRequest.reviewText(),
                    updateRequest.containsSpoilers(),
                    null,
                    null
            );

            when(reviewRepository.findById(review.getId()))
                    .thenReturn(Optional.of(review));

            when(reviewRepository.save(review))
                    .thenReturn(review);

            when(reviewMapper.toResponse(review))
                    .thenReturn(updateResponse);

            ReviewResponse result = reviewService.update(review.getId(), updateRequest);

            assertThat(result).isNotNull();
            assertThat(result.rating()).isEqualByComparingTo(updateRequest.rating());
            assertThat(result.reviewText()).isEqualTo(updateRequest.reviewText());
            assertThat(result.containsSpoilers()).isEqualTo(updateRequest.containsSpoilers());

            verify(reviewRepository).findById(review.getId());
            verify(reviewRepository).save(review);
            verify(reviewMapper).toResponse(review);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao atualizar com review inexistente")
        void shouldThrowNotFoundExceptionWhenReviewNonExistent() {
            UUID nonExistentReviewId = UUID.randomUUID();

            when(reviewRepository.findById(nonExistentReviewId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    reviewService.update(nonExistentReviewId, request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Review não encontrada");

            verify(reviewRepository).findById(nonExistentReviewId);
            verify(reviewRepository, never()).save(any());

            verifyNoInteractions(reviewMapper);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("Deve deletar review com sucesso")
        void shouldDeleteReviewSuccessfully() {
            when(reviewRepository.findById(review.getId()))
                    .thenReturn(Optional.of(review));

            reviewService.delete(review.getId());

            verify(reviewRepository).findById(review.getId());
            verify(reviewRepository).delete(review);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao tentar deletar review inexistente")
        void shouldThrowNotFoundExceptionWhenDeletingReviewNonExistent() {
            UUID nonExistentReviewId = UUID.randomUUID();

            when(reviewRepository.findById(nonExistentReviewId)).
                    thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    reviewService.delete(nonExistentReviewId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Review não encontrada");

            verify(reviewRepository).findById(nonExistentReviewId);
            verify(reviewRepository, never()).delete(any());
        }
    }
}