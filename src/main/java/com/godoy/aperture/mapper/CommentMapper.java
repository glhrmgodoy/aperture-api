package com.godoy.aperture.mapper;

import com.godoy.aperture.domain.entity.Comment;
import com.godoy.aperture.dto.request.CommentRequest;
import com.godoy.aperture.dto.response.CommentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentMapper {

    @Mapping(target = "review", ignore = true)
    @Mapping(target = "user", ignore = true)
    Comment toEntity(CommentRequest request);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    CommentResponse toResponse(Comment comment);
}
