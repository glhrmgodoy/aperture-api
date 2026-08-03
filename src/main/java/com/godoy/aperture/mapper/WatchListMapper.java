package com.godoy.aperture.mapper;

import com.godoy.aperture.domain.entity.WatchList;
import com.godoy.aperture.dto.response.WatchListResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface WatchListMapper {

    @Mapping(target = "movieId", source = "movie.id")
    @Mapping(target = "movieTitle", source = "movie.title")
    WatchListResponse toResponse(WatchList watchList);
}
