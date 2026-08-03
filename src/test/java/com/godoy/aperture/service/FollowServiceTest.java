package com.godoy.aperture.service;

import com.godoy.aperture.domain.entity.Follow;
import com.godoy.aperture.domain.entity.User;
import com.godoy.aperture.dto.response.FollowResponse;
import com.godoy.aperture.exception.BusinessException;
import com.godoy.aperture.exception.NotFoundException;
import com.godoy.aperture.mapper.FollowMapper;
import com.godoy.aperture.repository.FollowRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FollowMapper followMapper;

    @InjectMocks
    private FollowService followService;

    private User follower;
    private User following;
    private Follow follow;
    private FollowResponse response;

    @BeforeEach
    void setUp() {
        follower = User.builder()
                .id(UUID.randomUUID())
                .username("maria")
                .email("maria@email.com")
                .password("12345")
                .active(true)
                .build();

        following = User.builder()
                .id(UUID.randomUUID())
                .username("godoy")
                .email("godoy@email.com")
                .password("12345")
                .active(true)
                .build();

        follow = Follow.builder()
                .id(UUID.randomUUID())
                .follower(follower)
                .following(following)
                .build();

        response = new FollowResponse(
                follow.getId(),
                follower.getId(),
                follower.getUsername(),
                following.getId(),
                following.getUsername(),
                null
        );
    }

    @Nested
    @DisplayName("follow")
    class DoFollow {

        @Test
        @DisplayName("Deve seguir usuário com sucesso")
        void shouldFollowUserSuccessfully() {
            when(userRepository.findById(follower.getId()))
                    .thenReturn(Optional.of(follower));

            when(userRepository.findById(following.getId()))
                    .thenReturn(Optional.of(following));

            when(followRepository.existsByFollowerIdAndFollowingId(follower.getId(), following.getId()))
                    .thenReturn(false);

            when(followRepository.save(any(Follow.class)))
                    .thenReturn(follow);

            when(followMapper.toResponse(follow))
                    .thenReturn(response);

            FollowResponse result = followService.follow(follower.getId(), following.getId());

            ArgumentCaptor<Follow> captor = ArgumentCaptor.forClass(Follow.class);

            verify(followRepository).save(captor.capture());

            Follow captured = captor.getValue();

            assertThat(captured.getFollower()).isEqualTo(follower);
            assertThat(captured.getFollowing()).isEqualTo(following);

            assertThat(result).isNotNull();
            assertThat(result.followerId()).isEqualTo(follower.getId());
            assertThat(result.followingId()).isEqualTo(following.getId());

            verify(userRepository).findById(follower.getId());
            verify(userRepository).findById(following.getId());
            verify(followRepository).existsByFollowerIdAndFollowingId(follower.getId(), following.getId());
            verify(followMapper).toResponse(follow);
        }

        @Test
        @DisplayName("Deve lançar BusinessException ao tentar seguir a si mesmo")
        void shouldThrowBusinessExceptionWhenFollowingSelf() {
            assertThatThrownBy(() ->
                    followService.follow(follower.getId(), follower.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Você não pode seguir a si mesmo");

            verifyNoInteractions(userRepository);
            verifyNoInteractions(followRepository);
            verifyNoInteractions(followMapper);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando solicitante (follower) não existe")
        void shouldThrowNotFoundExceptionWhenFollowerDoesNotExist() {
            UUID nonExistentFollowerId = UUID.randomUUID();

            when(userRepository.findById(nonExistentFollowerId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    followService.follow(nonExistentFollowerId, following.getId()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Usuário não encontrado");

            verify(userRepository).findById(nonExistentFollowerId);
            verify(userRepository, never()).findById(following.getId());

            verifyNoInteractions(followRepository);
            verifyNoInteractions(followMapper);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando o usuário a ser seguido não existe")
        void shouldThrowNotFoundExceptionWhenFollowingUserDoesNotExist() {
            UUID nonExistentFollowingId = UUID.randomUUID();

            when(userRepository.findById(follower.getId()))
                    .thenReturn(Optional.of(follower));

            when(userRepository.findById(nonExistentFollowingId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    followService.follow(follower.getId(), nonExistentFollowingId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Usuário não encontrado");

            verify(userRepository).findById(follower.getId());
            verify(userRepository).findById(nonExistentFollowingId);

            verifyNoInteractions(followRepository);
            verifyNoInteractions(followMapper);
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando usuário já segue o outro")
        void shouldThrowBusinessExceptionWhenAlreadyFollowing() {
            when(userRepository.findById(follower.getId()))
                    .thenReturn(Optional.of(follower));

            when(userRepository.findById(following.getId()))
                    .thenReturn(Optional.of(following));

            when(followRepository.existsByFollowerIdAndFollowingId(follower.getId(), following.getId()))
                    .thenReturn(true);

            assertThatThrownBy(() ->
                    followService.follow(follower.getId(), following.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Você já segue este usuário");

            verify(userRepository).findById(follower.getId());
            verify(userRepository).findById(following.getId());
            verify(followRepository).existsByFollowerIdAndFollowingId(follower.getId(), following.getId());

            verify(followRepository, never()).save(any());
            verifyNoInteractions(followMapper);
        }
    }

    @Nested
    @DisplayName("unfollow")
    class Unfollow {

        @Test
        @DisplayName("Deve deixar de seguir usuário com sucesso")
        void shouldUnfollowUserSuccessfully() {
            when(followRepository.findByFollowerIdAndFollowingId(follower.getId(), following.getId()))
                    .thenReturn(Optional.of(follow));

            followService.unfollow(follower.getId(), following.getId());

            verify(followRepository).findByFollowerIdAndFollowingId(follower.getId(), following.getId());
            verify(followRepository).delete(follow);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao deixar de seguir usuário não seguido")
        void shouldThrowNotFoundExceptionWhenUnfollowingUserNotFollowed() {
            when(followRepository.findByFollowerIdAndFollowingId(follower.getId(), following.getId()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    followService.unfollow(follower.getId(), following.getId()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Você não segue este usuário");

            verify(followRepository).findByFollowerIdAndFollowingId(follower.getId(), following.getId());
            verify(followRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("findFollowers")
    class FindFollowers {

        @Test
        @DisplayName("Deve retornar lista de seguidores do usuário com sucesso")
        void shouldReturnListOfUserFollowersSuccessfully() {
            when(userRepository.findById(following.getId()))
                    .thenReturn(Optional.of(following));

            when(followRepository.findByFollowingId(following.getId()))
                    .thenReturn(List.of(follow));

            when(followMapper.toResponse(follow))
                    .thenReturn(response);

            List<FollowResponse> result = followService.findFollowers(following.getId());

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().followerId()).isEqualTo(follower.getId());
            assertThat(result.getFirst().followerUsername()).isEqualTo(follower.getUsername());

            verify(userRepository).findById(following.getId());
            verify(followRepository).findByFollowingId(following.getId());
            verify(followMapper).toResponse(follow);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao buscar seguidor com usuário inexistente")
        void shouldThrowNotFoundExceptionWhenFindingFollowersOfNonExistentUser() {
            UUID nonExistentFollowingId = UUID.randomUUID();

            when(userRepository.findById(nonExistentFollowingId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    followService.findFollowers(nonExistentFollowingId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Usuário não encontrado");

            verify(userRepository).findById(nonExistentFollowingId);

            verifyNoInteractions(followRepository);
            verifyNoInteractions(followMapper);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há seguidores")
        void shouldReturnEmptyListWhenThereAreNoFollowers() {
            when(userRepository.findById(following.getId()))
                    .thenReturn(Optional.of(following));

            when(followRepository.findByFollowingId(following.getId()))
                    .thenReturn(List.of());

            List<FollowResponse> result = followService.findFollowers(following.getId());

            assertThat(result).isEmpty();

            verify(userRepository).findById(following.getId());
            verify(followRepository).findByFollowingId(following.getId());

            verifyNoInteractions(followMapper);
        }
    }

    @Nested
    @DisplayName("findFollowing")
    class FindFollowing {

        @Test
        @DisplayName("Deve retornar lista de usuários seguidos com sucesso")
        void shouldReturnListOfUsersFollowedSuccessfully() {
            when(userRepository.findById(follower.getId()))
                    .thenReturn(Optional.of(follower));

            when(followRepository.findByFollowerId(follower.getId()))
                    .thenReturn(List.of(follow));

            when(followMapper.toResponse(follow))
                    .thenReturn(response);

            List<FollowResponse> result = followService.findFollowing(follower.getId());

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().followingId()).isEqualTo(following.getId());
            assertThat(result.getFirst().followingUsername()).isEqualTo(following.getUsername());

            verify(userRepository).findById(follower.getId());
            verify(followRepository).findByFollowerId(follower.getId());
            verify(followMapper).toResponse(follow);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao buscar quem é seguido com usuário inexistente")
        void shouldThrowNotFoundExceptionWhenFindingFollowingOfNonExistentUser() {
            UUID nonExistentFollowingId = UUID.randomUUID();

            when(userRepository.findById(nonExistentFollowingId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    followService.findFollowing(nonExistentFollowingId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Usuário não encontrado");

            verify(userRepository).findById(nonExistentFollowingId);
            verifyNoInteractions(followMapper);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não segue ninguém")
        void shouldReturnEmptyListWhenNoFollowingUsers() {
            when(userRepository.findById(follower.getId()))
                    .thenReturn(Optional.of(follower));

            when(followRepository.findByFollowerId(follower.getId()))
                    .thenReturn(List.of());

            List<FollowResponse> result = followService.findFollowing(follower.getId());

            assertThat(result).isEmpty();

            verify(userRepository).findById(follower.getId());
            verify(followRepository).findByFollowerId(follower.getId());

            verifyNoInteractions(followMapper);
        }
    }
}