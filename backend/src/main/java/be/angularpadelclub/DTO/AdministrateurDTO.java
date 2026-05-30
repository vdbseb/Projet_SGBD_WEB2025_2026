package be.angularpadelclub.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AdministrateurDTO(
        Integer id,

        @NotBlank(message = "Le matricule de l'administrateur est obligatoire.")
        String matricule,

        @NotBlank(message = "Le nom de l'administrateur est obligatoire.")
        String nom,

        @NotBlank(message = "Le prénom de l'administrateur est obligatoire.")
        String prenom,

        @NotBlank(message = "L'email de l'administrateur est obligatoire.")
        @Email(message = "L'email de l'administrateur doit être valide.")
        String email,

        @NotBlank(message = "Le type d'administrateur est obligatoire.")
        @Pattern(
                regexp = "GLOBAL|SITE",
                message = "Le type d'administrateur doit être GLOBAL ou SITE."
        )
        String typeAdmin,

        Integer siteId,

        String siteNom
) {
}