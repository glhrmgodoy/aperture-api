package com.godoy.aperture.service;

import com.godoy.aperture.domain.entity.Movie;
import com.godoy.aperture.domain.enums.Genre;
import com.godoy.aperture.dto.request.MovieRequest;
import com.godoy.aperture.dto.response.MovieResponse;
import com.godoy.aperture.exception.NotFoundException;
import com.godoy.aperture.mapper.MovieMapper;
import com.godoy.aperture.repository.MovieRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MovieMapper movieMapper;

    @InjectMocks
    private MovieService movieService;

    private Movie movie;
    private MovieRequest request;
    private MovieResponse response;

    @BeforeEach
    void setUp() {
        request = new MovieRequest(
                "Lawrence of Arabia",
                1962,
                "David Lean",
                "",
                "",
                228,
                List.of(Genre.ADVENTURE, Genre.WAR)
        );

        movie = Movie.builder()
                .id(UUID.randomUUID())
                .title(request.title())
                .releaseYear(request.releaseYear())
                .director(request.director())
                .synopsis(request.synopsis())
                .posterUrl(request.posterUrl())
                .runtimeMinutes(request.runtimeMinutes())
                .genres(request.genres())
                .active(true)
                .build();

        response = new MovieResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getReleaseYear(),
                movie.getDirector(),
                movie.getSynopsis(),
                movie.getPosterUrl(),
                movie.getRuntimeMinutes(),
                movie.getGenres(),
                null
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Deve criar filme com sucesso")
        void shouldCreateMovieSuccessfully() {
            when(movieMapper.toEntity(request))
                    .thenReturn(movie);

            when(movieRepository.save(movie))
                    .thenReturn(movie);

            when(movieMapper.toResponse(movie))
                    .thenReturn(response);

            MovieResponse result = movieService.create(request);

            assertThat(result).isNotNull();
            assertThat(result.title()).isEqualTo(request.title());
            assertThat(result.releaseYear()).isEqualTo(request.releaseYear());
            assertThat(result.director()).isEqualTo(request.director());
            assertThat(result.runTimeMinutes()).isEqualTo(request.runtimeMinutes());
            assertThat(result.genres()).isEqualTo(request.genres());

            verify(movieMapper).toEntity(request);
            verify(movieRepository).save(movie);
            verify(movieMapper).toResponse(movie);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Deve retornar filme por ID com sucesso")
        void shouldFindMovieById() {
            when(movieRepository.findById(movie.getId()))
                    .thenReturn(Optional.of(movie));

            when(movieMapper.toResponse(movie))
                    .thenReturn(response);

            MovieResponse result = movieService.findById(movie.getId());

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(movie.getId());
            assertThat(result.title()).isEqualTo(movie.getTitle());
            assertThat(result.releaseYear()).isEqualTo(movie.getReleaseYear());
            assertThat(result.director()).isEqualTo(movie.getDirector());
            assertThat(result.runTimeMinutes()).isEqualTo(movie.getRuntimeMinutes());
            assertThat(result.genres()).isEqualTo(movie.getGenres());

            verify(movieRepository).findById(movie.getId());
            verify(movieMapper).toResponse(movie);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando filme não encontrado")
        void shouldThrowNotFoundExceptionWhenMovieNotFound() {
            UUID nonExistentMovieId = UUID.randomUUID();

            when(movieRepository.findById(nonExistentMovieId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> movieService.findById(nonExistentMovieId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Filme não encontrado");

            verify(movieRepository).findById(nonExistentMovieId);

            verifyNoInteractions(movieMapper);
        }
    }

    @Nested
    @DisplayName("findByTitle")
    class FindByTitle {

        @Test
        @DisplayName("Deve retorna página de filmes por título com sucesso")
        void shouldReturnPageOfTitleMovies() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<Movie> page = new PageImpl<>(
                    List.of(movie),
                    pageable,
                    1
            );

            when(movieRepository.findByTitleContainingIgnoreCase(movie.getTitle(), pageable))
                    .thenReturn(page);

            when(movieMapper.toResponse(movie))
                    .thenReturn(response);

            Page<MovieResponse> result = movieService.findByTitle(movie.getTitle(), pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().id()).isEqualTo(movie.getId());
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getNumber()).isZero();

            verify(movieRepository).findByTitleContainingIgnoreCase(movie.getTitle(), pageable);
            verify(movieMapper).toResponse(movie);
        }

        @Test
        @DisplayName("Deve retornar página vazia quando título não encontrado")
        void shouldReturnEmptyPageOfTitleMovies() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<Movie> emptyPage = Page.empty(pageable);

            when(movieRepository.findByTitleContainingIgnoreCase(any(), any()))
                    .thenReturn(emptyPage);

            Page<MovieResponse> result = movieService.findByTitle(movie.getTitle(), pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();

            verify(movieRepository).findByTitleContainingIgnoreCase(movie.getTitle(), pageable);

            verifyNoInteractions(movieMapper);
        }
    }

    @Nested
    @DisplayName("findByGenre")
    class FindByGenre {

        @Test
        @DisplayName("Deve retornar página de filmes por gênero com sucesso")
        void shouldReturnPageOfGenreMovies() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<Movie> page = new PageImpl<>(
                    List.of(movie),
                    pageable,
                    1
            );

            when(movieRepository.findByGenresContaining(Genre.ADVENTURE, pageable))
                    .thenReturn(page);

            when(movieMapper.toResponse(movie))
                    .thenReturn(response);

            Page<MovieResponse> result = movieService.findByGenre(Genre.ADVENTURE, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().id()).isEqualTo(movie.getId());
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getNumber()).isZero();

            verify(movieRepository).findByGenresContaining(Genre.ADVENTURE, pageable);
            verify(movieMapper).toResponse(movie);
        }

        @Test
        @DisplayName("Deve retornar página vazia quando filme por gênero não encontrado")
        void shouldReturnEmptyPageOfGenreMovies() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<Movie> emptyPage = Page.empty(pageable);

            when(movieRepository.findByGenresContaining(any(), any()))
                    .thenReturn(emptyPage);

            Page<MovieResponse> result = movieService.findByGenre(Genre.ADVENTURE, pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();

            verify(movieRepository).findByGenresContaining(any(), any());

            verifyNoInteractions(movieMapper);
        }
    }

    @Nested
    @DisplayName("findByReleaseYear")
    class FindByReleaseYear {

        @Test
        @DisplayName("Deve retornar página de filmes lançados em determinado ano")
        void shouldReturnPageOfReleaseYearMovies() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<Movie> page = new PageImpl<>(
                    List.of(movie),
                    pageable,
                    1
            );

            when(movieRepository.findByReleaseYear(movie.getReleaseYear(), pageable))
                    .thenReturn(page);

            when(movieMapper.toResponse(movie))
                    .thenReturn(response);

            Page<MovieResponse> result = movieService.findByReleaseYear(movie.getReleaseYear(), pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().id()).isEqualTo(movie.getId());
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getNumber()).isZero();

            verify(movieRepository).findByReleaseYear(movie.getReleaseYear(), pageable);
            verify(movieMapper).toResponse(movie);
        }

        @Test
        @DisplayName("Deve retornar página vazia quando filme em determinado ano não encontrado")
        void shouldReturnEmptyPageOfReleaseYearMovies() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<Movie> emptyPage = Page.empty(pageable);

            when(movieRepository.findByReleaseYear(movie.getReleaseYear(), pageable))
                    .thenReturn(emptyPage);

            Page<MovieResponse> result = movieService.findByReleaseYear(movie.getReleaseYear(), pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();

            verify(movieRepository).findByReleaseYear(movie.getReleaseYear(), pageable);

            verifyNoInteractions(movieMapper);
        }
    }

    @Nested
    @DisplayName("findByDirector")
    class FindByDirector {

        @Test
        @DisplayName("Deve retornar página de filmes por determinado diretor")
        void shouldReturnPageOfDirectorMovies() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<Movie> page = new PageImpl<>(
                    List.of(movie),
                    pageable,
                    1
            );

            when(movieRepository.findByDirectorContainingIgnoreCase(movie.getDirector(), pageable))
                    .thenReturn(page);

            when(movieMapper.toResponse(movie))
                    .thenReturn(response);

            Page<MovieResponse> result = movieService.findByDirector(movie.getDirector(), pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getNumber()).isZero();

            verify(movieRepository).findByDirectorContainingIgnoreCase(movie.getDirector(), pageable);
            verify(movieMapper).toResponse(movie);
        }

        @Test
        @DisplayName("Deve retornar página vazia quando filmes de determinado diretor não encontrado")
        void shouldReturnEmptyPageOfDirectorMovies() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<Movie> emptyPage = Page.empty(pageable);

            when(movieRepository.findByDirectorContainingIgnoreCase(movie.getDirector(), pageable))
                    .thenReturn(emptyPage);

            Page<MovieResponse> result = movieService.findByDirector(movie.getDirector(), pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();

            verify(movieRepository).findByDirectorContainingIgnoreCase(movie.getDirector(), pageable);

            verifyNoInteractions(movieMapper);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("Deve atualizar filme com sucesso")
        void shouldUpdateMovieSuccessfully() {
            MovieRequest updateRequest = new MovieRequest(
                    "Lawrence of Arabia",
                    1962,
                    "David Lean",
                    "",
                    "",
                    228,
                    List.of(Genre.ADVENTURE, Genre.WAR, Genre.HISTORY)
            );

            MovieResponse updateResponse = new MovieResponse(
                    movie.getId(),
                    updateRequest.title(),
                    updateRequest.releaseYear(),
                    updateRequest.director(),
                    updateRequest.synopsis(),
                    updateRequest.posterUrl(),
                    updateRequest.runtimeMinutes(),
                    updateRequest.genres(),
                    null
            );

            when(movieRepository.findById(movie.getId()))
                    .thenReturn(Optional.of(movie));

            when(movieRepository.save(movie))
                    .thenReturn(movie);

            when(movieMapper.toResponse(movie))
                    .thenReturn(updateResponse);

            MovieResponse result = movieService.update(movie.getId(), updateRequest);

            assertThat(result).isNotNull();
            assertThat(result.title()).isEqualTo(updateRequest.title());
            assertThat(result.releaseYear()).isEqualTo(updateRequest.releaseYear());
            assertThat(result.director()).isEqualTo(updateRequest.director());
            assertThat(result.runTimeMinutes()).isEqualTo(updateRequest.runtimeMinutes());
            assertThat(result.genres()).isEqualTo(updateRequest.genres());

            verify(movieRepository).findById(movie.getId());
            verify(movieRepository).save(movie);
            verify(movieMapper).toResponse(movie);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao atualizar filme inexistente")
        void shouldThrowNotFoundExceptionWhenUpdatingNonExistentMovie() {
            UUID nonExistentMovieId = UUID.randomUUID();

            when(movieRepository.findById(nonExistentMovieId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> movieService.update(nonExistentMovieId, request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Filme não encontrado");

            verify(movieRepository).findById(nonExistentMovieId);
            verify(movieRepository, never()).save(any());

            verifyNoInteractions(movieMapper);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("Deve inativar filme com sucesso")
        void shouldDeleteMovieSuccessfully() {
            when(movieRepository.findById(movie.getId()))
                    .thenReturn(Optional.of(movie));

            movieService.delete(movie.getId());

            assertThat(movie.getActive()).isFalse();

            verify(movieRepository).findById(movie.getId());
            verify(movieRepository).save(movie);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao inativar filme inexistente")
        void shouldThrowNotFoundExceptionWhenDeletingNonExistentMovie() {
            UUID nonExistentMovieId = UUID.randomUUID();

            when(movieRepository.findById(nonExistentMovieId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> movieService.delete(nonExistentMovieId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Filme não encontrado");

            verify(movieRepository).findById(nonExistentMovieId);
            verify(movieRepository, never()).save(any());
        }
    }
}