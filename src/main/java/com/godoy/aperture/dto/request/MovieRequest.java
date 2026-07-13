package com.godoy.aperture.dto.request;

import com.godoy.aperture.domain.enums.Genre;
import jakarta.validation.constraints.*;

import java.util.List;

public record MovieRequest(

        @NotBlank(message = "Título é obrigatório")
        String title,

        @NotNull(message = "Ano de lançamento é obrigatório")
        Integer releaseYear,

        @NotBlank(message = "Diretor é obrigatório")
        String director,

        @NotBlank(message = "Sinopse é obrigatório")
        @Size(max = 600)
        String synopsis,

        String posterUrl,

        @NotNull(message = "Duração é obrigatória")
        @Positive
        Integer runTimeMinutes,

        @NotEmpty(message = "Gênero é obrigatório")
        List<Genre> genres
) {
}
