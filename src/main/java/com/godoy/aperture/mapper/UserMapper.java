package com.godoy.aperture.mapper;

import com.godoy.aperture.domain.entity.User;
import com.godoy.aperture.dto.request.UserRequest;
import com.godoy.aperture.dto.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    User toEntity(UserRequest request);

    UserResponse toResponse(User user);
}
