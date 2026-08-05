package com.godoy.aperture.mapper;

import com.godoy.aperture.domain.entity.ListItem;
import com.godoy.aperture.dto.request.ListItemRequest;
import com.godoy.aperture.dto.response.ListItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ListItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customList", ignore = true)
    @Mapping(target = "movie", ignore = true)
    @Mapping(target = "addedAt", ignore = true)
    ListItem toEntity(ListItemRequest request);

    @Mapping(target = "movieId", source = "movie.id")
    @Mapping(target = "movieTitle", source = "movie.title")
    ListItemResponse toResponse(ListItem listItem);
}
