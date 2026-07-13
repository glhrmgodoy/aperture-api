package com.godoy.aperture.mapper;

import com.godoy.aperture.domain.entity.Movie;
import com.godoy.aperture.dto.request.MovieRequest;
import com.godoy.aperture.dto.response.MovieResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MovieMapper {

    Movie toEntity(MovieRequest request);

    MovieResponse toResponse(Movie movie);
}
