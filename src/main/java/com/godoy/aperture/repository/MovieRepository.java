package com.godoy.aperture.repository;

import com.godoy.aperture.domain.entity.Movie;
import com.godoy.aperture.domain.enums.Genre;
import org.antlr.v4.runtime.misc.Interval;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MovieRepository extends JpaRepository<Movie, UUID> {

    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Movie> findByReleaseYear(Integer releaseYear, Pageable pageable);

    Page<Movie> findByGenresContaining(Genre genre, Pageable pageable);

    Page<Movie> findByDirectorContainingIgnoreCase(String director, Pageable pageable);
}
