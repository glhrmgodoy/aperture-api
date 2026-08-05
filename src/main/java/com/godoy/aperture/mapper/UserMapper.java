package com.godoy.aperture.mapper;

import com.godoy.aperture.domain.entity.User;
import com.godoy.aperture.dto.request.UserRequest;
import com.godoy.aperture.dto.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "customLists", ignore = true)
    @Mapping(target = "following", ignore = true)
    @Mapping(target = "followers", ignore = true)
    @Mapping(target = "watchLists", ignore = true)
    User toEntity(UserRequest request);

    UserResponse toResponse(User user);
}
