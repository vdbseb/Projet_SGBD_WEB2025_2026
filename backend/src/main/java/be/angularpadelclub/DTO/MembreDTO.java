package be.angularpadelclub.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record MembreDTO(
        Integer id,

        boolean active,

        @NotBlank(message = "L'email du membre est obligatoire.")
        @Email(message = "L'email du membre doit être valide.")
        String email,

        @NotBlank(message = "Le matricule du membre est obligatoire.")
        @Pattern(
                regexp = "^[GSL][0-9]{4,5}$",
                message = "Le matricule doit commencer par G, S ou L et être suivi de chiffres."
        )
        String matricule,

        @NotBlank(message = "Le prénom du membre est obligatoire.")
        String firstName,

        @NotBlank(message = "Le nom du membre est obligatoire.")
        String lastName,

        @NotBlank(message = "Le type de membre est obligatoire.")
        String typeCode,

        Integer siteId,

        String siteName
) {
}