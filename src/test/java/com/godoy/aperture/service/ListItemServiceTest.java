package com.godoy.aperture.service;

import com.godoy.aperture.domain.entity.CustomList;
import com.godoy.aperture.domain.entity.ListItem;
import com.godoy.aperture.domain.entity.Movie;
import com.godoy.aperture.domain.entity.User;
import com.godoy.aperture.domain.enums.Genre;
import com.godoy.aperture.domain.enums.ListVisibility;
import com.godoy.aperture.dto.request.ListItemRequest;
import com.godoy.aperture.dto.response.ListItemResponse;
import com.godoy.aperture.exception.BusinessException;
import com.godoy.aperture.exception.NotFoundException;
import com.godoy.aperture.mapper.ListItemMapper;
import com.godoy.aperture.repository.CustomListRepository;
import com.godoy.aperture.repository.ListItemRepository;
import com.godoy.aperture.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class ListItemServiceTest {

    @Mock
    private ListItemRepository listItemRepository;

    @Mock
    private CustomListRepository customListRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private ListItemMapper listItemMapper;

    @Mock
    private CustomListService customListService;

    @InjectMocks
    private ListItemService listItemService;

    private User owner;
    private Movie movie;
    private CustomList customList;
    private ListItem listItem;
    private ListItemRequest request;
    private ListItemResponse response;

    @BeforeEach
    void setUp() {
        owner = User.builder()
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

        customList = CustomList.builder()
                .id(UUID.randomUUID())
                .user(owner)
                .name("IMBb Top 250")
                .description("")
                .visibility(ListVisibility.PUBLIC)
                .ranked(false)
                .build();

        request = new ListItemRequest(
                movie.getId(),
                null
        );

        listItem = ListItem.builder()
                .id(UUID.randomUUID())
                .customList(customList)
                .movie(movie)
                .position(request.position())
                .build();

        response = new ListItemResponse(
                listItem.getId(),
                movie.getId(),
                movie.getTitle(),
                listItem.getPosition(),
                null
        );
    }

    @Nested
    @DisplayName("addMovie")
    class AddMovie {

        @Test
        @DisplayName("Deve adicionar filme na lista com sucesso")
        void shouldAddMovieInListSuccessfully() {
            when(customListRepository.findById(customList.getId()))
                    .thenReturn(Optional.of(customList));

            doNothing().when(customListService)
                    .validateOwner(customList, owner.getId());

            when(movieRepository.findById(request.movieId()))
                    .thenReturn(Optional.of(movie));

            when(listItemRepository.existsByCustomListIdAndMovieId(customList.getId(), movie.getId()))
                    .thenReturn(false);

            when(listItemMapper.toEntity(request))
                    .thenReturn(listItem);

            when(listItemRepository.save(listItem))
                    .thenReturn(listItem);

            when(listItemMapper.toResponse(listItem))
                    .thenReturn(response);

            ListItemResponse result = listItemService.addMovie(customList.getId(), owner.getId(), request);

            assertThat(result).isNotNull();
            assertThat(result.movieId()).isEqualTo(movie.getId());
            assertThat(result.position()).isEqualTo(listItem.getPosition());

            verify(customListRepository).findById(customList.getId());
            verify(customListService).validateOwner(customList, owner.getId());
            verify(movieRepository).findById(request.movieId());
            verify(listItemRepository).existsByCustomListIdAndMovieId(customList.getId(), movie.getId());
            verify(listItemRepository).save(listItem);
            verify(listItemMapper).toEntity(request);
            verify(listItemMapper).toResponse(listItem);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando lista não encontrada")
        void shouldThrowNotFoundExceptionWhenListNotFound() {
            UUID nonExistentListId = UUID.randomUUID();

            when(customListRepository.findById(nonExistentListId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    listItemService.addMovie(nonExistentListId, owner.getId(), request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Lista não encontrada");



            verify(customListRepository).findById(nonExistentListId);

            verifyNoInteractions(listItemRepository, listItemMapper);
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando solicitante não for o dono da lista")
        void shouldThrowBusinessExceptionWhenRequesterIsNotOwner() {
            when(customListRepository.findById(customList.getId()))
                    .thenReturn(Optional.of(customList));

            doThrow(new BusinessException("Você não tem permissão para alterar essa lista"))
                    .when(customListService)
                    .validateOwner(customList, owner.getId());

            assertThatThrownBy(() ->
                    listItemService.addMovie(customList.getId(), owner.getId(), request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Você não tem permissão para alterar essa lista");

            verify(customListRepository).findById(customList.getId());
            verify(customListService).validateOwner(customList, owner.getId());

            verifyNoInteractions(listItemRepository, listItemMapper);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando filme não encontrado")
        void shouldThrowNotFoundExceptionWhenMovieNotFound() {
            when(customListRepository.findById(customList.getId()))
                    .thenReturn(Optional.of(customList));

            doNothing().when(customListService)
                    .validateOwner(customList, owner.getId());

            when(movieRepository.findById(request.movieId()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    listItemService.addMovie(customList.getId(), owner.getId(), request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Filme não encontrado");

            verify(movieRepository).findById(request.movieId());
            verify(customListService).validateOwner(customList, owner.getId());
            verify(movieRepository).findById(request.movieId());

            verifyNoInteractions(listItemRepository, listItemMapper);
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando filme já está na lista")
        void shouldThrowBusinessExceptionWhenMovieAlreadyExistsInList() {
            when(customListRepository.findById(customList.getId()))
                    .thenReturn(Optional.of(customList));

            doNothing().when(customListService)
                    .validateOwner(customList, owner.getId());

            when(movieRepository.findById(request.movieId()))
                    .thenReturn(Optional.of(movie));

            when(listItemRepository.existsByCustomListIdAndMovieId(customList.getId(), movie.getId()))
                    .thenReturn(true);

            assertThatThrownBy(() ->
                    listItemService.addMovie(customList.getId(), owner.getId(), request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Este filme já está na lista");

            verify(customListRepository).findById(customList.getId());
            verify(customListService).validateOwner(customList, owner.getId());
            verify(movieRepository).findById(request.movieId());
            verify(listItemRepository).existsByCustomListIdAndMovieId(customList.getId(), movie.getId());
            verify(listItemRepository, never()).save(any());

            verifyNoInteractions(listItemMapper);
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando lista for ranqueada e posição do filme não foi informada")
        void shouldThrowBusinessExceptionWhenRankedListHasNoPosition() {
            customList.setRanked(true);

            when(customListRepository.findById(customList.getId()))
                    .thenReturn(Optional.of(customList));

            doNothing().when(customListService)
                    .validateOwner(customList, owner.getId());

            when(movieRepository.findById(request.movieId()))
                    .thenReturn(Optional.of(movie));

            when(listItemRepository.existsByCustomListIdAndMovieId(customList.getId(), movie.getId()))
                    .thenReturn(false);

            assertThatThrownBy(() ->
                    listItemService.addMovie(customList.getId(), owner.getId(), request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Esta é uma lista ranqueada, informe a posição do filme");

            verify(customListRepository).findById(customList.getId());
            verify(customListService).validateOwner(customList, owner.getId());
            verify(movieRepository).findById(request.movieId());
            verify(listItemRepository).existsByCustomListIdAndMovieId(customList.getId(), movie.getId());
            verify(listItemRepository, never()).save(any());

            verifyNoInteractions(listItemMapper);
        }

        @Test
        @DisplayName("Deve adicionar filme com sucesso em lista ranqueada quando posição é informada")
        void shouldAddMovieSuccessfullyWhenRankedListWithPosition() {
            customList.setRanked(true);

            ListItemRequest rankedRequest = new ListItemRequest(movie.getId(), 1);

            ListItemResponse rankedResponse = new ListItemResponse(
                    listItem.getId(),
                    movie.getId(),
                    movie.getTitle(),
                    rankedRequest.position(),
                    null
            );

            when(customListRepository.findById(customList.getId()))
                    .thenReturn(Optional.of(customList));

            doNothing().when(customListService)
                    .validateOwner(customList, owner.getId());

            when(movieRepository.findById(rankedRequest.movieId()))
                    .thenReturn(Optional.of(movie));

            when(listItemRepository.existsByCustomListIdAndMovieId(customList.getId(), movie.getId()))
                    .thenReturn(false);

            when(listItemMapper.toEntity(rankedRequest))
                    .thenReturn(listItem);

            when(listItemRepository.save(listItem))
                    .thenReturn(listItem);

            when(listItemMapper.toResponse(listItem))
                    .thenReturn(rankedResponse);

            ListItemResponse result = listItemService.addMovie(customList.getId(), owner.getId(), rankedRequest);

            assertThat(result).isNotNull();
            assertThat(result.movieId()).isEqualTo(rankedRequest.movieId());
            assertThat(result.position()).isEqualTo(rankedRequest.position());

            verify(customListRepository).findById(customList.getId());
            verify(customListService).validateOwner(customList, owner.getId());
            verify(movieRepository).findById(rankedRequest.movieId());
            verify(listItemRepository).existsByCustomListIdAndMovieId(customList.getId(), movie.getId());
            verify(listItemMapper).toEntity(rankedRequest);
            verify(listItemRepository).save(listItem);
            verify(listItemMapper).toResponse(listItem);
        }
    }

    @Nested
    @DisplayName("findByListId")
    class FindByListId {

        @Test
        @DisplayName("Deve retornar itens da lista por ID com sucesso")
        void shouldReturnListItemsSuccessfully() {
            when(customListRepository.findById(customList.getId()))
                    .thenReturn(Optional.of(customList));

            when(listItemRepository.findByCustomListIdOrderByPosition(customList.getId()))
                    .thenReturn(List.of(listItem));

            when(listItemMapper.toResponse(listItem))
                    .thenReturn(response);

            List<ListItemResponse> result = listItemService.findByListId(customList.getId());

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().movieId()).isEqualTo(movie.getId());
            assertThat(result.getFirst().id()).isEqualTo(listItem.getId());

            verify(customListRepository).findById(customList.getId());
            verify(listItemRepository).findByCustomListIdOrderByPosition(customList.getId());
            verify(listItemMapper).toResponse(listItem);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando lista não encontrada")
        void shouldThrowNotFoundExceptionWhenListNotFound() {
            UUID nonExistentListId = UUID.randomUUID();

            when(customListRepository.findById(nonExistentListId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    listItemService.findByListId(nonExistentListId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Lista não encontrada");

            verify(listItemRepository, never()).findByCustomListIdOrderByPosition(nonExistentListId);

            verifyNoInteractions(listItemMapper);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não houver itens")
        void shouldReturnEmptyListWhenListHasNoItems() {
            when(customListRepository.findById(customList.getId()))
                    .thenReturn(Optional.of(customList));

            when(listItemRepository.findByCustomListIdOrderByPosition(customList.getId()))
                    .thenReturn(List.of());

            List<ListItemResponse> result = listItemService.findByListId(customList.getId());

            assertThat(result).isNotNull();
            assertThat(result).isEmpty();

            verify(customListRepository).findById(customList.getId());
            verify(listItemRepository).findByCustomListIdOrderByPosition(customList.getId());

            verifyNoInteractions(listItemMapper);
        }
    }

    @Nested
    @DisplayName("removeMovie")
    class RemoveMovie {

        @Test
        @DisplayName("Deve remover filme da lista com  sucesso")
        void shouldRemoveMovieSuccessfully() {
            when(customListRepository.findById(customList.getId()))
                    .thenReturn(Optional.of(customList));

            doNothing().when(customListService)
                    .validateOwner(customList, owner.getId());

            when(listItemRepository.findByCustomListIdAndMovieId(customList.getId(), movie.getId()))
                    .thenReturn(Optional.of(listItem));

            listItemService.removeMovie(customList.getId(), movie.getId(), owner.getId());

            verify(customListRepository).findById(customList.getId());
            verify(customListService).validateOwner(customList, owner.getId());
            verify(listItemRepository).findByCustomListIdAndMovieId(customList.getId(), movie.getId());
            verify(listItemRepository).delete(listItem);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao remover filme de lista inexistente")
        void shouldThrowNotFoundExceptionWhenRemoveMovieInInexistentList() {
            UUID nonExistentListId = UUID.randomUUID();

            when(customListRepository.findById(nonExistentListId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    listItemService.removeMovie(nonExistentListId, movie.getId(), owner.getId()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Lista não encontrada");

            verify(customListRepository).findById(nonExistentListId);

            verifyNoInteractions(listItemRepository, customListService);
        }

        @Test
        @DisplayName("Deve lançar BusinessException ao remover filme não sendo o dono da lista")
        void shouldThrowBusinessExceptionWhenRemovingMovieAsNonOwner() {
            UUID otherUserId = UUID.randomUUID();

            when(customListRepository.findById(customList.getId()))
                    .thenReturn(Optional.of(customList));

            doThrow(new BusinessException("Você não tem permissão para alterar essa lista"))
                    .when(customListService)
                    .validateOwner(customList, otherUserId);

            assertThatThrownBy(() ->
                    listItemService.removeMovie(customList.getId(), movie.getId(), otherUserId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Você não tem permissão para alterar essa lista");

            verify(customListRepository).findById(customList.getId());
            verify(customListService).validateOwner(customList, otherUserId);

            verifyNoInteractions(listItemRepository);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao tentar remover filme que não está na lista")
        void shouldThrowNotFoundExceptionWhenTryRemovingMovieNotInList() {
            when(customListRepository.findById(customList.getId()))
                    .thenReturn(Optional.of(customList));

            doNothing().when(customListService)
                    .validateOwner(customList, owner.getId());

            when(listItemRepository.findByCustomListIdAndMovieId(customList.getId(), movie.getId()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    listItemService.removeMovie(customList.getId(), movie.getId(), owner.getId()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Filme não está nesta lista");

            verify(customListRepository).findById(customList.getId());
            verify(customListService).validateOwner(customList, owner.getId());
            verify(listItemRepository).findByCustomListIdAndMovieId(customList.getId(), movie.getId());
            verify(listItemRepository, never()).delete(any());
        }
    }
}