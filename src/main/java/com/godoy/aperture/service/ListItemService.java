package com.godoy.aperture.service;

import com.godoy.aperture.domain.entity.CustomList;
import com.godoy.aperture.domain.entity.ListItem;
import com.godoy.aperture.domain.entity.Movie;
import com.godoy.aperture.dto.request.ListItemRequest;
import com.godoy.aperture.dto.response.ListItemResponse;
import com.godoy.aperture.exception.BusinessException;
import com.godoy.aperture.exception.NotFoundException;
import com.godoy.aperture.mapper.ListItemMapper;
import com.godoy.aperture.repository.CustomListRepository;
import com.godoy.aperture.repository.ListItemRepository;
import com.godoy.aperture.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListItemService {

    private final ListItemRepository listItemRepository;
    private final CustomListRepository customListRepository;
    private final MovieRepository movieRepository;
    private final ListItemMapper listItemMapper;
    private final CustomListService customListService;

    public ListItemResponse addMovie(UUID customListId, UUID authenticatedUserId, ListItemRequest request) {
        CustomList customList = customListRepository.findById(customListId)
                .orElseThrow(() -> new NotFoundException("Lista não encontrada"));

        customListService.validateOwner(customList, authenticatedUserId);

        Movie movie = movieRepository.findById(request.movieId())
                .orElseThrow(() -> new NotFoundException("Filme não encontrado"));

        if (listItemRepository.existsByCustomListIAndMovieId(customListId, movie.getId())) {
            throw new BusinessException("Este filme já está na lista");
        }

        if (customList.getRanked() && request.position() == null) {
            throw new BusinessException("Esta é uma lista ranqueada, informe a posição do filme");
        }

        ListItem listItem = listItemMapper.toEntity(request);
        listItem.setCustomList(customList);
        listItem.setMovie(movie);

        ListItem saved = listItemRepository.save(listItem);

        return listItemMapper.toResponse(saved);
    }

    public List<ListItemResponse> findByList(UUID customListId) {
        customListRepository.findById(customListId)
                .orElseThrow(() -> new NotFoundException("Lista não encontrada"));

        return listItemRepository.findByCustomListIdOrderByPosition(customListId)
                .stream()
                .map(listItemMapper::toResponse)
                .toList();
    }

    public void removeMovie(UUID customListId, UUID movieId, UUID authenticatedUserId) {
        CustomList customList = customListRepository.findById(customListId)
                .orElseThrow(() -> new NotFoundException("Lista não encontrada"));

        customListService.validateOwner(customList, authenticatedUserId);

        ListItem listItem = listItemRepository.findByCustomListIdAndMovieId(customListId, movieId)
                .orElseThrow(() -> new NotFoundException("Filme não está nesta lista"));

        listItemRepository.delete(listItem);
    }
}
