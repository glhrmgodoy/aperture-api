package com.godoy.aperture.service;

import com.godoy.aperture.domain.entity.Movie;
import com.godoy.aperture.domain.entity.User;
import com.godoy.aperture.domain.entity.WatchList;
import com.godoy.aperture.domain.enums.Genre;
import com.godoy.aperture.dto.response.WatchListResponse;
import com.godoy.aperture.exception.BusinessException;
import com.godoy.aperture.exception.NotFoundException;
import com.godoy.aperture.mapper.WatchListMapper;
import com.godoy.aperture.repository.MovieRepository;
import com.godoy.aperture.repository.UserRepository;
import com.godoy.aperture.repository.WatchListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WatchListServiceTest {

    @Mock
    private WatchListRepository watchListRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private WatchListMapper watchListMapper;

    @InjectMocks
    private WatchListService watchListService;

    private User user;
    private Movie movie;
    private WatchList watchList;
    private WatchListResponse response;

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
                .title("Lawrence of Arabia")
                .releaseYear(1962)
                .director("David Lean")
                .runtimeMinutes(228)
                .genres(List.of(Genre.ADVENTURE, Genre.WAR))
                .active(true)
                .build();

        watchList = WatchList.builder()
                .id(UUID.randomUUID())
                .user(user)
                .movie(movie)
                .build();

        response = new WatchListResponse(
                watchList.getId(),
                movie.getId(),
                movie.getTitle(),
                null
        );
    }

    @Nested
    @DisplayName("addMovie")
    class AddMovie {

        @Test
        @DisplayName("Deve adicionar filme com sucesso")
        void shouldAddMovie() {
            when(userRepository.findById(user.getId()))
                    .thenReturn(Optional.of(user));

            when(movieRepository.findById(movie.getId()))
                    .thenReturn(Optional.of(movie));

            when(watchListRepository.existsByUserIdAndMovieId(user.getId(), movie.getId()))
                    .thenReturn(false);

            when(watchListRepository.save(any(WatchList.class)))
                    .thenReturn(watchList);

            when(watchListMapper.toResponse(watchList))
                    .thenReturn(response);

            WatchListResponse result = watchListService.addMovie(user.getId(), movie.getId());

            ArgumentCaptor<WatchList> captor = ArgumentCaptor.forClass(WatchList.class);

            verify(watchListRepository).save(captor.capture());

            WatchList captured = captor.getValue();

            assertThat(captured.getUser()).isEqualTo(user);
            assertThat(captured.getMovie()).isEqualTo(movie);

            assertThat(result).isNotNull();
            assertThat(result.movieId()).isEqualTo(movie.getId());
            assertThat(result.movieTitle()).isEqualTo(movie.getTitle());

            verify(userRepository).findById(user.getId());
            verify(movieRepository).findById(movie.getId());
            verify(watchListRepository).existsByUserIdAndMovieId(user.getId(), movie.getId());
            verify(watchListMapper).toResponse(watchList);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao adicionar filme para usuário inexistente")
        void shouldThrowNotFoundExceptionWhenAddingMovieForNonExistentUser() {
            UUID nonExistentUserId = UUID.randomUUID();

            when(userRepository.findById(nonExistentUserId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    watchListService.addMovie(nonExistentUserId, movie.getId()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Usuário não encontrado");

            verify(userRepository).findById(nonExistentUserId);

            verifyNoInteractions(movieRepository, watchListRepository, watchListMapper);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao adicionar filme inexistente na watchlist")
        void shouldThrowNotFoundExceptionWhenAddingNonExistentMovie() {
            UUID nonExistentMovieId = UUID.randomUUID();

            when(userRepository.findById(user.getId()))
                    .thenReturn(Optional.of(user));

            when(movieRepository.findById(nonExistentMovieId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    watchListService.addMovie(user.getId(), nonExistentMovieId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Filme não encontrado");


            verify(userRepository).findById(user.getId());
            verify(movieRepository).findById(nonExistentMovieId);

            verifyNoInteractions(watchListRepository, watchListMapper);
        }

        @Test
        @DisplayName("Deve lançar BusinessException ao adicionar filme já presente na watchlist")
        void shouldThrowBusinessExceptionWhenMovieAlreadyInWatchlist() {
            when(userRepository.findById(user.getId()))
                    .thenReturn(Optional.of(user));

            when(movieRepository.findById(movie.getId()))
                    .thenReturn(Optional.of(movie));

            when(watchListRepository.existsByUserIdAndMovieId(user.getId(), movie.getId()))
                    .thenReturn(true);

            assertThatThrownBy(() ->
                    watchListService.addMovie(user.getId(), movie.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Esse filme já está em sua WatchList");

            verify(userRepository).findById(user.getId());
            verify(movieRepository).findById(movie.getId());
            verify(watchListRepository).existsByUserIdAndMovieId(user.getId(), movie.getId());
            verify(watchListRepository, never()).save(any());

            verifyNoInteractions(watchListMapper);
        }
    }

    @Nested
    @DisplayName("findByUserId")
    class findByUserId {

        @Test
        @DisplayName("Deve retornar watchlist do usuário com sucesso")
        void shouldReturnUserWatchListSuccessfully() {
            when(userRepository.findById(user.getId()))
                    .thenReturn(Optional.of(user));

            when(watchListRepository.findByUserId(user.getId()))
                    .thenReturn(List.of(watchList));

            when(watchListMapper.toResponse(watchList))
                    .thenReturn(response);

            List<WatchListResponse> result = watchListService.findByUserId(user.getId());

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().id()).isEqualTo(watchList.getId());
            assertThat(result.getFirst().movieId()).isEqualTo(movie.getId());
            assertThat(result.getFirst().movieTitle()).isEqualTo(movie.getTitle());

            verify(userRepository).findById(user.getId());
            verify(watchListRepository).findByUserId(user.getId());
            verify(watchListMapper).toResponse(watchList);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao buscar watchlist de usuário inexistente")
        void shouldThrowNotFoundExceptionWhenFindingWatchListOfNonExistentUser() {
            UUID nonExistentUserId = UUID.randomUUID();

            when(userRepository.findById(nonExistentUserId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    watchListService.findByUserId(nonExistentUserId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Usuário não encontrado");

            verify(userRepository).findById(nonExistentUserId);

            verifyNoInteractions(watchListRepository, watchListMapper);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando usuário não tem filmes em sua watchlist")
        void shouldReturnEmptyListWhenUserHasNoWatchlistMovies() {
            when(userRepository.findById(user.getId()))
                    .thenReturn(Optional.of(user));

            when(watchListRepository.findByUserId(user.getId()))
                    .thenReturn(List.of());

            List<WatchListResponse> result = watchListService.findByUserId(user.getId());

            assertThat(result).isEmpty();

            verify(userRepository).findById(user.getId());
            verify(watchListRepository).findByUserId(user.getId());

            verifyNoInteractions(watchListMapper);
        }
    }

    @Nested
    @DisplayName("removeMovie")
    class removeMovie {

        @Test
        @DisplayName("Deve remover filme da watchlist com sucesso")
        void shouldRemoveMovieFromWatchListSuccessfully() {
            when(watchListRepository.findByUserIdAndMovieId(user.getId(), movie.getId()))
                    .thenReturn(Optional.of(watchList));

            watchListService.removeMovie(user.getId(), movie.getId());

            verify(watchListRepository).findByUserIdAndMovieId(user.getId(), movie.getId());
            verify(watchListRepository).delete(watchList);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao tentar remover filme que não está na watchlist")
        void shouldThrowNotFoundExceptionWhenRemovingMovieNotInWatchlist() {
            when(watchListRepository.findByUserIdAndMovieId(user.getId(), movie.getId()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    watchListService.removeMovie(user.getId(), movie.getId()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Filme não está em sua WatchList");

            verify(watchListRepository).findByUserIdAndMovieId(user.getId(), movie.getId());
            verify(watchListRepository, never()).delete(any());
        }
    }
}