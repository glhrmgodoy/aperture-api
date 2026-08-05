package com.godoy.aperture.mapper;

import com.godoy.aperture.domain.entity.CustomList;
import com.godoy.aperture.dto.request.CustomListRequest;
import com.godoy.aperture.dto.response.CustomListResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CustomListMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "listItems", ignore = true)
    CustomList toEntity(CustomListRequest request);

    @Mapping(target = "userId", source = "user.id")
    CustomListResponse toResponse(CustomList customList);
}
