package com.godoy.aperture.service;

import com.godoy.aperture.domain.entity.Movie;
import com.godoy.aperture.domain.entity.User;
import com.godoy.aperture.domain.entity.WatchList;
import com.godoy.aperture.dto.response.WatchListResponse;
import com.godoy.aperture.exception.BusinessException;
import com.godoy.aperture.exception.NotFoundException;
import com.godoy.aperture.mapper.WatchListMapper;
import com.godoy.aperture.repository.MovieRepository;
import com.godoy.aperture.repository.UserRepository;
import com.godoy.aperture.repository.WatchListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WatchListService {

    private final WatchListRepository watchListRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final WatchListMapper watchListMapper;

    public WatchListResponse addMovie(UUID userId, UUID movieId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new NotFoundException("Filme não encontrado"));

        if (watchListRepository.existsByUserIdAndMovieId(userId, movieId)) {
            throw new BusinessException("Esse filme já está em sua WatchList");
        }

        WatchList watchList = new WatchList();
        watchList.setUser(user);
        watchList.setMovie(movie);

        WatchList saved = watchListRepository.save(watchList);

        return watchListMapper.toResponse(saved);
    }

    public List<WatchListResponse> findByUserId(UUID userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        return watchListRepository.findByUserId(userId)
                .stream()
                .map(watchListMapper::toResponse)
                .toList();
    }

    public void removeMovie(UUID userId, UUID movieId) {
        WatchList watchList = watchListRepository.findByUserIdAndMovieId(userId, movieId)
                .orElseThrow(() -> new NotFoundException("Filme não está em sua WatchList"));

        watchListRepository.delete(watchList);
    }
}
