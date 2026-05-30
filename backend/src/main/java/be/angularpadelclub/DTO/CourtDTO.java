package be.angularpadelclub.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CourtDTO(
        Integer id,

        @NotBlank(message = "Le nom du terrain est obligatoire.")
        String name,

        @NotNull(message = "Le site du terrain est obligatoire.")
        Integer siteId,

        boolean indoor,

        boolean active,

        boolean maintenance
) {
}