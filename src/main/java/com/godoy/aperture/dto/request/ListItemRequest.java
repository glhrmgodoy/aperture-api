package com.godoy.aperture.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ListItemRequest(

        @NotNull(message = "ID do filme é obrigatório")
        UUID movieId,

        Integer position
) {
}
