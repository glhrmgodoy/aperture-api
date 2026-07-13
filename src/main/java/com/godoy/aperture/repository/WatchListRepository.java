package com.godoy.aperture.repository;

import com.godoy.aperture.domain.entity.WatchList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WatchListRepository extends JpaRepository<WatchList, UUID> {

    List<WatchList> findByUserId(UUID userId);

    Optional<WatchList> findByUserIdAndMovieId(UUID userId, UUID movieId);

    boolean existsByUserIdAndMovieId(UUID userId, UUID movieId);
}
