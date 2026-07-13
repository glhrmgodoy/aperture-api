package com.godoy.aperture.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 30)
        String username,

        @Email
        @NotBlank(message = "Email é obrigatório")
        String email,

        @NotBlank(message = "Senha é obrigatório")
        @Size(min = 6)
        String password,

        @Size(min = 300)
        String bio,

        String avatarUrl
) {
}
