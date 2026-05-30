package be.angularpadelclub.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record JourFermetureDTO(
        Integer id,

        Integer siteId,

        String siteNom,

        @NotNull(message = "La date de fermeture est obligatoire.")
        LocalDate dateFermeture,

        @NotBlank(message = "La raison de la fermeture est obligatoire.")
        @Size(max = 255, message = "La raison de la fermeture ne peut pas dépasser 255 caractères.")
        String raison,

        boolean global
) {
}