package com.godoy.aperture.service;

import com.godoy.aperture.domain.entity.Follow;
import com.godoy.aperture.domain.entity.User;
import com.godoy.aperture.dto.response.FollowResponse;
import com.godoy.aperture.exception.BusinessException;
import com.godoy.aperture.exception.NotFoundException;
import com.godoy.aperture.mapper.FollowMapper;
import com.godoy.aperture.repository.FollowRepository;
import com.godoy.aperture.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final FollowMapper followMapper;

    public FollowResponse follow(UUID followerId, UUID followingId) {
        if (followerId.equals(followingId)) {
            throw new BusinessException("Você não pode seguir a si mesmo");
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new BusinessException("Você já segue este usuário");
        }

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);

        Follow saved = followRepository.save(follow);

        return followMapper.toResponse(saved);
    }

    public void unfollow(UUID followerId, UUID followingId) {
        Follow follow = followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                .orElseThrow(() -> new NotFoundException("Você não segue este usuário"));

        followRepository.delete(follow);
    }

    public List<FollowResponse> findFollowers(UUID userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        return followRepository.findByFollowingId(userId)
                .stream()
                .map(followMapper::toResponse)
                .toList();
    }

    public List<FollowResponse> findFollowing(UUID userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        return followRepository.findByFollowerId(userId)
                .stream()
                .map(followMapper::toResponse)
                .toList();
    }
}
