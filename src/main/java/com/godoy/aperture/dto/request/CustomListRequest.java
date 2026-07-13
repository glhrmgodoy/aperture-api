package com.godoy.aperture.dto.request;

import com.godoy.aperture.domain.enums.ListVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CustomListRequest(

        @NotNull(message = "ID do usuário é obrigatório")
        UUID userId,

        @NotBlank(message = "Nome é obrigatório")
        String name,

        String description,

        @NotNull(message = "Visibilidade da lista é obrigatório")
        ListVisibility visibility,

        Boolean ranked
) {
}
