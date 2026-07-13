package com.godoy.aperture.mapper;

import com.godoy.aperture.domain.entity.Review;
import com.godoy.aperture.dto.request.ReviewRequest;
import com.godoy.aperture.dto.response.ReviewResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReviewMapper {

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "movie", ignore = true)
    Review toEntity(ReviewRequest request);

    @Mapping(source = "userId", target = "user.id")
    @Mapping(source = "username", target = "user.username")
    @Mapping(source = "movieId", target = "movie.id")
    @Mapping(source = "movieTitle", target = "movie.title")
    ReviewResponse toResponse(Review review);
}
