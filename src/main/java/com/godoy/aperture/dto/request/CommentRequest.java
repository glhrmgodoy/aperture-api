package com.godoy.aperture.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequest(
        @NotBlank(message = "Texto é obrigatório")
        @Size(max = 500)
        String text
) {
}
