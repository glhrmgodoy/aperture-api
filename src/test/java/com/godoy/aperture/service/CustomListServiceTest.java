package com.godoy.aperture.service;

import com.godoy.aperture.domain.entity.CustomList;
import com.godoy.aperture.domain.entity.User;
import com.godoy.aperture.domain.enums.ListVisibility;
import com.godoy.aperture.dto.request.CustomListRequest;
import com.godoy.aperture.dto.response.CustomListResponse;
import com.godoy.aperture.exception.BusinessException;
import com.godoy.aperture.exception.NotFoundException;
import com.godoy.aperture.mapper.CustomListMapper;
import com.godoy.aperture.repository.CustomListRepository;
import com.godoy.aperture.repository.UserRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomListServiceTest {

    @Mock
    private CustomListRepository customListRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomListMapper customListMapper;

    @InjectMocks
    private CustomListService customListService;

    private User user;
    private CustomList customList;
    private CustomListRequest request;
    private CustomListResponse response;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .username("username")
                .email("email")
                .password("password")
                .active(true)
                .build();

        request = new CustomListRequest(
                user.getId(),
                "Oscar 2026",
                "Filmes indicados ao Oscar de 2026",
                ListVisibility.PUBLIC,
                false
        );

        customList = CustomList.builder()
                .id(UUID.randomUUID())
                .user(user)
                .name(request.name())
                .description(request.description())
                .visibility(request.visibility())
                .ranked(request.ranked())
                .build();

        response = new CustomListResponse(
                customList.getId(),
                user.getId(),
                customList.getName(),
                customList.getDescription(),
                customList.getVisibility(),
                customList.getRanked(),
                null
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Deve criar lista com sucesso")
        void shouldCreateCustomListSuccessfully() {
            when(userRepository.findById(request.userId()))
                    .thenReturn(Optional.of(user));

            when(customListMapper.toEntity(request))
                    .thenReturn(customList);

            when(customListRepository.save(customList))
                    .thenReturn(customList);

            when(customListMapper.toResponse(customList))
                    .thenReturn(response);

            CustomListResponse result = customListService.create(request);

            assertThat(result).isNotNull();
            assertThat(result.userId()).isEqualTo(user.getId());
            assertThat(result.name()).isEqualTo(request.name());
            assertThat(result.description()).isEqualTo(request.description());
            assertThat(result.visibility()).isEqualTo(request.visibility());
            assertThat(result.ranked()).isEqualTo(request.ranked());

            verify(userRepository).findById(request.userId());
            verify(customListRepository).save(customList);
            verify(customListMapper).toEntity(request);
            verify(customListMapper).toResponse(customList);
        }

        @Test
        @DisplayName("Deve retornar NotFoundException quando usuário não encontrado")
        void shouldThrowNotFoundExceptionWhenUserNotFound() {
            when(userRepository.findById(request.userId()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    customListService.create(request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Usuário não encontrado");

            verify(userRepository).findById(request.userId());

            verifyNoInteractions(customListRepository, customListMapper);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Deve retornar lista por ID com sucesso")
        void shouldFindCustomListByIdSuccessfully() {
            when(customListRepository.findById(customList.getId()))
                    .thenReturn(Optional.of(customList));

            when(customListMapper.toResponse(customList))
                    .thenReturn(response);

            CustomListResponse result = customListService.findById(customList.getId());

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(customList.getId());
            assertThat(result.name()).isEqualTo(customList.getName());
            assertThat(result.description()).isEqualTo(customList.getDescription());
            assertThat(result.visibility()).isEqualTo(customList.getVisibility());
            assertThat(result.ranked()).isEqualTo(customList.getRanked());

            verify(customListRepository).findById(customList.getId());
            verify(customListMapper).toResponse(customList);
        }

        @Test
        @DisplayName("Deve retornar NotFoundException quando lista não encontrada")
        void shouldThrowNotFoundExceptionWhenCustomListNotFound() {
            UUID nonExistentListId = UUID.randomUUID();

            when(customListRepository.findById(nonExistentListId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    customListService.findById(nonExistentListId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Lista não encontrada");

            verify(customListRepository).findById(nonExistentListId);

            verifyNoInteractions(customListMapper);
        }
    }

    @Nested
    @DisplayName("findPublicLists")
    class FindPublicLists {

        @Test
        @DisplayName("Deve retornar página de listas públicas com sucesso")
        void shouldReturnPageOfPublicListsSuccessfully() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<CustomList> page = new PageImpl<>(
                    List.of(customList),
                    pageable,
                    1
            );

            when(customListRepository.findByVisibility(ListVisibility.PUBLIC, pageable))
                    .thenReturn(page);

            when(customListMapper.toResponse(customList))
                    .thenReturn(response);

            Page<CustomListResponse> result = customListService.findPublicLists(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().id()).isEqualTo(customList.getId());
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getNumber()).isZero();

            verify(customListRepository).findByVisibility(ListVisibility.PUBLIC, pageable);
            verify(customListMapper).toResponse(customList);
        }

        @Test
        @DisplayName("Deve retornar página vazia quando não houver listas públicas")
        void shouldReturnEmptyPageWhenNoPublicLists() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<CustomList> emptyPage = Page.empty(pageable);

            when(customListRepository.findByVisibility(ListVisibility.PUBLIC, pageable))
                    .thenReturn(emptyPage);

            Page<CustomListResponse> result = customListService.findPublicLists(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getTotalPages()).isZero();

            verify(customListRepository).findByVisibility(ListVisibility.PUBLIC, pageable);

            verifyNoInteractions(customListMapper);
        }
    }

    @Nested
    @DisplayName("findByUserId")
    class findByUserId {

        @Test
        @DisplayName("Deve retornar página de listas de um usuário com sucesso")
        void shouldReturnPageOfUserListsSuccessfully() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<CustomList> page = new PageImpl<>(
                    List.of(customList),
                    pageable,
                    1
            );

            when(userRepository.findById(user.getId()))
                    .thenReturn(Optional.of(user));

            when(customListRepository.findByUserId(user.getId(), pageable))
                    .thenReturn(page);

            when(customListMapper.toResponse(customList))
                    .thenReturn(response);

            Page<CustomListResponse> result = customListService.findByUserId(user.getId(), pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().id()).isEqualTo(customList.getId());
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getNumber()).isZero();

            verify(userRepository).findById(user.getId());
            verify(customListRepository).findByUserId(user.getId(), pageable);
            verify(customListMapper).toResponse(customList);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao prucurar lista de usuário inexistente")
        void shouldThrowNotFoundExceptionWhenListOwnerUserNonExistent() {
            UUID nonExistentUserId = UUID.randomUUID();

            Pageable pageable = PageRequest.of(0, 10);

            when(userRepository.findById(nonExistentUserId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    customListService.findByUserId(nonExistentUserId, pageable))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Usuário não encontrado");

            verify(userRepository).findById(nonExistentUserId);

            verifyNoInteractions(customListMapper);
        }

        @Test
        @DisplayName("Deve retornar página vazia quando usuário não possuir listas")
        void shouldReturnEmptyPageWhenUserNonLists() {
            Pageable pageable = PageRequest.of(0, 10);

            Page<CustomList> emptyPage = Page.empty(pageable);

            when(userRepository.findById(user.getId()))
                    .thenReturn(Optional.of(user));

            when(customListRepository.findByUserId(user.getId(), pageable))
                    .thenReturn(emptyPage);

            Page<CustomListResponse> result = customListService.findByUserId(user.getId(), pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getTotalPages()).isZero();

            verify(customListRepository).findByUserId(user.getId(), pageable);
            verify(customListRepository).findByUserId(user.getId(), pageable);

            verifyNoInteractions(customListMapper);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("Deve atualizar lista com sucesso quando solicitante é o dono")
        void shouldUpdateListSuccessfully() {
            CustomListRequest updateRequest = new CustomListRequest(
                    user.getId(),
                    "Top 100 Horror Films",
                    "Melhores filmes de horror",
                    ListVisibility.PRIVATE,
                    true
            );

            CustomListResponse updateResponse = new CustomListResponse(
                    customList.getId(),
                    user.getId(),
                    updateRequest.name(),
                    updateRequest.description(),
                    updateRequest.visibility(),
                    updateRequest.ranked(),
                    null
            );

            when(customListRepository.findById(customList.getId()))
                    .thenReturn(Optional.of(customList));

            when(customListRepository.save(any(CustomList.class)))
                    .thenReturn(customList);

            when(customListMapper.toResponse(customList))
                    .thenReturn(updateResponse);

            CustomListResponse result = customListService.update(customList.getId(), user.getId(), updateRequest);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo(updateRequest.name());
            assertThat(result.description()).isEqualTo(updateRequest.description());
            assertThat(result.visibility()).isEqualTo(updateRequest.visibility());
            assertThat(result.ranked()).isEqualTo(updateRequest.ranked());

            verify(customListRepository).findById(customList.getId());
            verify(customListRepository).save(customList);
            verify(customListMapper).toResponse(customList);
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando solicitante da atualização não é o dono")
        void shouldThrowBusinessExceptionWhenRequesterUpdatingIsNotOwner() {
            UUID otherUserId = UUID.randomUUID();

            when(customListRepository.findById(customList.getId()))
                    .thenReturn(Optional.of(customList));

            assertThatThrownBy(() ->
                    customListService.update(customList.getId(), otherUserId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Usuário não tem permissão para alterar essa lista");

            verify(customListRepository).findById(customList.getId());
            verify(customListRepository, never()).save(any());

            verifyNoInteractions(customListMapper);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao atualizar lista inexistente")
        void shouldThrowNotFoundExceptionWhenUpdatingListNonExistent() {
            UUID nonExistentListId = UUID.randomUUID();

            when(customListRepository.findById(nonExistentListId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    customListService.update(nonExistentListId, user.getId(), request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Lista não encontrada");

            verify(customListRepository, never()).save(any());
            verifyNoInteractions(customListMapper);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("Deve deletar lista com sucesso quando solicitante é o dono")
        void shouldDeleteListSuccessfully() {
            when(customListRepository.findById(customList.getId()))
                    .thenReturn(Optional.of(customList));

            customListService.delete(customList.getId(), user.getId());

            verify(customListRepository).findById(customList.getId());
            verify(customListRepository).delete(customList);
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando solicitante da deleção não é o dono")
        void shouldThrowBusinessExceptionWhenRequesterDeletingIsNotOwner() {
            UUID otherUserId = UUID.randomUUID();

            when(customListRepository.findById(customList.getId()))
                    .thenReturn(Optional.of(customList));

            assertThatThrownBy(() ->
                    customListService.delete(customList.getId(), otherUserId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Usuário não tem permissão para alterar essa lista");

            verify(customListRepository).findById(customList.getId());
            verify(customListRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao deletar lista inexistente")
        void shouldThrowNotFoundExceptionWhenDeletingListNonExistent() {
            UUID nonExistentListId = UUID.randomUUID();

            when(customListRepository.findById(nonExistentListId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    customListService.delete(nonExistentListId, user.getId()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Lista não encontrada");

            verify(customListRepository).findById(nonExistentListId);
            verify(customListRepository, never()).delete(any());
        }
    }
}