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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomListService {

    private final CustomListRepository customListRepository;
    private final UserRepository userRepository;
    private final CustomListMapper customListMapper;

    public CustomListResponse create(CustomListRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        CustomList customList = customListMapper.toEntity(request);
        customList.setUser(user);

        CustomList saved = customListRepository.save(customList);

        return customListMapper.toResponse(saved);
    }

    public Page<CustomListResponse> findPublicLists(Pageable pageable) {
        return customListRepository.findByVisibility(ListVisibility.PUBLIC, pageable)
                .map(customListMapper::toResponse);
    }

    public Page<CustomListResponse> findByUserId(UUID userId, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        return customListRepository.findByUserId(userId, pageable)
                .map(customListMapper::toResponse);
    }

    public CustomListResponse findById(UUID id) {
        CustomList customList = customListRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lista não encontrada"));

        return customListMapper.toResponse(customList);
    }

    public CustomListResponse update(UUID id, UUID authenticatedUserId, CustomListRequest request) {
        CustomList customList = customListRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lista não encontrada"));

        validateOwner(customList, authenticatedUserId);

        customList.setName(request.name());
        customList.setDescription(request.description());
        customList.setVisibility(request.visibility());
        customList.setRanked(request.ranked());

        CustomList updated = customListRepository.save(customList);

        return customListMapper.toResponse(updated);
    }

    public void delete(UUID id, UUID authenticatedUserId) {
        CustomList customList = customListRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lista não encontrada"));

        validateOwner(customList, authenticatedUserId);

        customListRepository.delete(customList);
    }

    public void validateOwner(CustomList customList, UUID authenticatedUserId) {
        if (!customList.getUser().getId().equals(authenticatedUserId)) {
            throw new BusinessException("Usuário não tem permissão para alterar essa lista");
        }
    }
}
