package be.angularpadelclub.DTO;

import jakarta.validation.constraints.NotBlank;

public record AdminLoginRequestDTO(
        @NotBlank(message = "Le matricule administrateur est obligatoire.")
        String matricule,

        @NotBlank(message = "Le mot de passe administrateur est obligatoire.")
        String password
) {
}