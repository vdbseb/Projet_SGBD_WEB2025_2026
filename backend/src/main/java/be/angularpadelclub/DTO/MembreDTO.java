package be.angularpadelclub.DTO;

import be.angularpadelclub.Entity.TypeMembreEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MembreDTO(
        Integer id,

        boolean active,

        @NotBlank(message = "L'email du membre est obligatoire.")
        @Email(message = "L'email du membre doit être valide.")
        String email,

        @NotBlank(message = "Le matricule du membre est obligatoire.")
        String matricule,

        @NotBlank(message = "Le prénom du membre est obligatoire.")
        String firstName,

        @NotBlank(message = "Le nom du membre est obligatoire.")
        String lastName,

        @NotNull(message = "Le type de membre est obligatoire.")
        TypeMembreEntity type,

        Integer siteId,

        String siteName
) {
}