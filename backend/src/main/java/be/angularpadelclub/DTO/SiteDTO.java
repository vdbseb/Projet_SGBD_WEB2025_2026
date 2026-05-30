package be.angularpadelclub.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;
import java.util.List;

public record SiteDTO(
        Integer id,

        @NotBlank(message = "Le nom du site est obligatoire.")
        String name,

        @NotBlank(message = "La ville du site est obligatoire.")
        String city,

        @NotBlank(message = "L'adresse du site est obligatoire.")
        String adresse,

        @NotBlank(message = "Le code postal du site est obligatoire.")
        String codePostal,

        @Size(max = 1000, message = "La description du site ne peut pas dépasser 1000 caractères.")
        String description,

        @NotNull(message = "L'heure d'ouverture du site est obligatoire.")
        LocalTime openingTime,

        @NotNull(message = "L'heure de fermeture du site est obligatoire.")
        LocalTime closingTime,

        @NotNull(message = "Le statut actif du site est obligatoire.")
        Boolean active,

        String imageUrl,

        List<@Valid CourtDTO> courts
) {
}