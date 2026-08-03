package com.godoy.aperture.service;

import com.godoy.aperture.domain.entity.User;
import com.godoy.aperture.dto.request.UserRequest;
import com.godoy.aperture.dto.response.UserResponse;
import com.godoy.aperture.exception.BusinessException;
import com.godoy.aperture.exception.NotFoundException;
import com.godoy.aperture.mapper.UserMapper;
import com.godoy.aperture.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponse create(UserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException("Usuário já cadastrado");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email já cadastrado");
        }

        User user = userMapper.toEntity(request);
        User saved = userRepository.save(user);

        return userMapper.toResponse(saved);
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    public UserResponse findById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        return userMapper.toResponse(user);
    }

    public UserResponse update(UUID id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        if (!user.getUsername().equals(request.username()) &&
                userRepository.existsByUsername(request.username())) {
            throw new BusinessException("Usuário já cadastrado");
        }

        if (!user.getEmail().equals(request.email()) &&
                userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email já cadastrado");
        }

        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(request.password());
        user.setBio(request.bio());
        user.setAvatarUrl(request.avatarUrl());

        User updated = userRepository.save(user);

        return userMapper.toResponse(updated);
    }

    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        user.setActive(false);
        userRepository.save(user);
    }
}
