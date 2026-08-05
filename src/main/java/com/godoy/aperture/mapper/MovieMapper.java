package com.godoy.aperture.mapper;

import com.godoy.aperture.domain.entity.Movie;
import com.godoy.aperture.dto.request.MovieRequest;
import com.godoy.aperture.dto.response.MovieResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MovieMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "listItems", ignore = true)
    @Mapping(target = "watchLists", ignore = true)
    Movie toEntity(MovieRequest request);

    MovieResponse toResponse(Movie movie);
}
