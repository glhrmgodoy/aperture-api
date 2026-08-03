package com.godoy.aperture.service;

import com.godoy.aperture.domain.entity.User;
import com.godoy.aperture.dto.request.UserRequest;
import com.godoy.aperture.dto.response.UserResponse;
import com.godoy.aperture.exception.BusinessException;
import com.godoy.aperture.exception.NotFoundException;
import com.godoy.aperture.mapper.UserMapper;
import com.godoy.aperture.repository.UserRepository;
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
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserRequest request;
    private UserResponse response;

    @BeforeEach
    void setUp() {
        request = new UserRequest(
                "maria.clara",
                "maria.clara@email.com",
                "Maria123",
                "",
                ""
        );

        user = User.builder()
                .id(UUID.randomUUID())
                .username(request.username())
                .email(request.email())
                .password(request.password())
                .bio(request.bio())
                .avatarUrl(request.avatarUrl())
                .active(true)
                .build();

        response = new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getBio(),
                user.getAvatarUrl(),
                null
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Deve criar usuário com sucesso")
        void shouldCreateUserSuccessfully() {
            when(userRepository.existsByUsername(request.username()))
                    .thenReturn(false);

            when(userRepository.existsByEmail(request.email()))
                    .thenReturn(false);

            when(userMapper.toEntity(request))
                    .thenReturn(user);

            when(userRepository.save(user))
                    .thenReturn(user);

            when(userMapper.toResponse(user))
                    .thenReturn(response);

            UserResponse result = userService.create(request);

            assertThat(result).isNotNull();
            assertThat(result.email()).isEqualTo(request.email());
            assertThat(result.username()).isEqualTo(request.username());

            verify(userRepository).existsByUsername(request.username());
            verify(userRepository).existsByEmail(request.email());
            verify(userRepository).save(user);
            verify(userMapper).toEntity(request);
            verify(userMapper).toResponse(user);
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando usuário já cadastrado")
        void shouldThrowBusinessExceptionWhenUsernameAlreadyExists() {
            when(userRepository.existsByUsername(request.username()))
                    .thenReturn(true);

            assertThatThrownBy(() ->
                    userService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Usuário já cadastrado");

            verify(userRepository, never()).save(any());
            verifyNoInteractions(userMapper);
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando email já cadastrado")
        void shouldThrowBusinessExceptionWhenEmailAlreadyExists() {
            when(userRepository.existsByEmail(request.email())).thenReturn(true);

            assertThatThrownBy(() -> userService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Email já cadastrado");

            verify(userRepository, never()).save(any());
            verifyNoInteractions(userMapper);
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("Deve retornar lista de usuários")
        void shouldReturnListOfUsers() {
            when(userRepository.findAll())
                    .thenReturn(List.of(user));

            when(userMapper.toResponse(user))
                    .thenReturn(response);

            List<UserResponse> result = userService.findAll();

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().id()).isEqualTo(user.getId());
            assertThat(result.getFirst().username()).isEqualTo(user.getUsername());
            assertThat(result.getFirst().email()).isEqualTo(user.getEmail());

            verify(userRepository).findAll();
            verify(userMapper).toResponse(user);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há usuários")
        void shouldReturnEmptyListWhenNoUsers() {
            when(userRepository.findAll())
                    .thenReturn(List.of());

            List<UserResponse> result = userService.findAll();

            assertThat(result).isNotNull();
            assertThat(result).isEmpty();

            verify(userRepository).findAll();

            verifyNoInteractions(userMapper);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Deve retornar usuário por ID com sucesso")
        void shouldFindUserByIdSuccessfully() {
            when(userRepository.findById(user.getId()))
                    .thenReturn(Optional.of(user));

            when(userMapper.toResponse(user))
                    .thenReturn(response);

            UserResponse result = userService.findById(user.getId());

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(user.getId());
            assertThat(result.username()).isEqualTo(user.getUsername());
            assertThat(result.email()).isEqualTo(user.getEmail());

            verify(userRepository).findById(user.getId());
            verify(userMapper).toResponse(user);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando usuário não encontrado")
        void shouldThrowNotFoundExceptionWhenUserNotFound() {
            UUID nonExistentUserId = UUID.randomUUID();

            when(userRepository.findById(nonExistentUserId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.findById(nonExistentUserId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Usuário não encontrado");

            verify(userRepository).findById(nonExistentUserId);
            verifyNoInteractions(userMapper);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("Deve atualizar usuário com sucesso")
        void shouldUpdateUserSuccessfully() {
            UserRequest updateRequest = new UserRequest(
                    "username",
                    "email@email.com",
                    "password",
                    "",
                    ""
            );

            UserResponse updateResponse = new UserResponse(
                    user.getId(),
                    updateRequest.username(),
                    updateRequest.email(),
                    updateRequest.bio(),
                    updateRequest.avatarUrl(),
                    null
            );

            when(userRepository.findById(user.getId()))
                    .thenReturn(Optional.of(user));

            when(userRepository.existsByUsername(updateRequest.username()))
                    .thenReturn(false);

            when(userRepository.existsByEmail(updateRequest.email()))
                    .thenReturn(false);

            when(userRepository.save(user))
                    .thenReturn(user);

            when(userMapper.toResponse(user))
                    .thenReturn(updateResponse);

            UserResponse result = userService.update(user.getId(), updateRequest);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(user.getId());
            assertThat(result.username()).isEqualTo(updateRequest.username());
            assertThat(result.email()).isEqualTo(updateRequest.email());

            assertThat(user.getUsername()).isEqualTo(updateRequest.username());
            assertThat(user.getEmail()).isEqualTo(updateRequest.email());
            assertThat(user.getPassword()).isEqualTo(updateRequest.password());
            assertThat(user.getBio()).isEqualTo(updateRequest.bio());
            assertThat(user.getAvatarUrl()).isEqualTo(updateRequest.avatarUrl());

            verify(userRepository).findById(user.getId());
            verify(userRepository).existsByUsername(updateRequest.username());
            verify(userRepository).existsByEmail(updateRequest.email());
            verify(userRepository).save(user);
            verify(userMapper).toResponse(user);
        }

        @Test
        @DisplayName("Deve lançar NotFoundxception quando ao atualizar usuário inexistente")
        void shouldThrowNotFoundExceptionWhenUpdatingNonExistentUser() {
            UUID nonExistentUserId = UUID.randomUUID();

            when(userRepository.findById(nonExistentUserId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.update(nonExistentUserId, request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Usuário não encontrado");

            verify(userRepository, never()).save(any());
            verifyNoInteractions(userMapper);
        }

        @Test
        @DisplayName("Deve lançar BusinessException ao atualizar username já usado por outro usuário")
        void shouldThrowBusinessExceptionWhenUpdatingUsernameUsedByAnotherUser() {
            user.setUsername("maria_clara");

            when(userRepository.findById(user.getId()))
                    .thenReturn(Optional.of(user));

            when(userRepository.existsByUsername(request.username()))
                    .thenReturn(true);

            assertThatThrownBy(() -> userService.update(user.getId(), request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Usuário já cadastrado");

            verify(userRepository, never()).save(any());
            verifyNoInteractions(userMapper);
        }

        @Test
        @DisplayName("Deve lançar BusinessException ao atualizar email já usado por outro usuário")
        void shouldThrowBusinessExceptionWhenUpdatingEmailUsedByAnotherUser() {
            user.setEmail("novo.email@email.com");

            when(userRepository.findById(user.getId()))
                    .thenReturn(Optional.of(user));

            when(userRepository.existsByEmail(request.email()))
                    .thenReturn(true);

            assertThatThrownBy(() -> userService.update(user.getId(), request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Email já cadastrado");

            verify(userRepository, never()).save(any());
            verifyNoInteractions(userMapper);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("Deve inativar usuário com sucesso")
        void shouldDeleteUserSuccessfully() {
            when(userRepository.findById(user.getId()))
                    .thenReturn(Optional.of(user));

            userService.delete(user.getId());

            assertThat(user.getActive()).isFalse();

            verify(userRepository).findById(user.getId());
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao inativar usuário inexistente")
        void shouldThrowNotFoundExceptionWhenDeletingNonExistentUser() {
            UUID nonExistentUserId = UUID.randomUUID();

            when(userRepository.findById(nonExistentUserId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.delete(nonExistentUserId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Usuário não encontrado");

            verify(userRepository).findById(nonExistentUserId);
            verify(userRepository, never()).save(any());
        }
    }
}