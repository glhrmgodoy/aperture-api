package com.godoy.aperture.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record ReviewRequest(

        @NotNull(message = "ID do usuário é obrigatória")
        UUID userId,

        @NotNull(message = "ID do filme é obrigatória")
        UUID movieId,

        @NotNull(message = "Nota é obrigatória")
        @DecimalMin(value = "0.5")
        @DecimalMax(value = "5.0")
        BigDecimal rating,

        @Size(max = 2000)
        String reviewText,

        Boolean containsSpoilers
) {
}
