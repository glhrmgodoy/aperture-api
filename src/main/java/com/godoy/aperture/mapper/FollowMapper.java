package com.godoy.aperture.mapper;

import com.godoy.aperture.domain.entity.Follow;
import com.godoy.aperture.dto.response.FollowResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FollowMapper {

    @Mapping(target = "followerId", source = "follower.id")
    @Mapping(target = "followerUsername", source = "follower.username")
    @Mapping(target = "followingId", source = "following.id")
    @Mapping(target = "followingUsername", source = "following.username")
    FollowResponse toResponse(Follow follow);
}
