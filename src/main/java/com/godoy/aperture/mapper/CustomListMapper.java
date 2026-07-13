package com.godoy.aperture.mapper;

import com.godoy.aperture.domain.entity.CustomList;
import com.godoy.aperture.dto.request.CustomListRequest;
import com.godoy.aperture.dto.response.CustomListResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CustomListMapper {

    @Mapping(target = "user", ignore = true)
    CustomList toEntity(CustomListRequest request);

    @Mapping(source = "userId", target = "user.id")
    CustomListResponse toResponse(CustomList customList);
}
