package com.godoy.aperture.repository;

import com.godoy.aperture.domain.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    Page<Review> findByMovieId(UUID movieId, Pageable pageable);

    Page<Review> findByUserId(UUID userId, Pageable pageable);

    boolean existsByUserIdAndMovieId(UUID userId, UUID movieId);
}
