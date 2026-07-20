package com.godoy.aperture.repository;

import com.godoy.aperture.domain.entity.ListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ListItemRepository extends JpaRepository<ListItem, UUID> {

    List<ListItem> findByCustomListIdOrderByPosition(UUID customListId);

    Optional<ListItem> findByCustomListIdAndMovieId(UUID customListId, UUID movieId);

    boolean existsByCustomListIAndMovieId(UUID customListId, UUID movieId);
}
