package com.godoy.aperture.mapper;

import com.godoy.aperture.domain.entity.Follow;
import com.godoy.aperture.dto.response.FollowResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FollowMapper {

    @Mapping(source = "followerId", target = "follower.id")
    @Mapping(source = "followerUsername", target = "follower.username")
    @Mapping(source = "followingId", target = "following.id")
    @Mapping(source = "followingUsername", target = "following.username")
    FollowResponse toResponse(Follow follow);
}
