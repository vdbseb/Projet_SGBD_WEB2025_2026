package be.angularpadelclub.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record HoraireSiteDTO(
        Integer id,

        @NotNull(message = "Le site de l'horaire est obligatoire.")
        Integer siteId,

        String siteNom,

        @Min(value = 2020, message = "L'année doit être supérieure ou égale à 2020.")
        @Max(value = 2100, message = "L'année doit être inférieure ou égale à 2100.")
        int annee,

        @NotNull(message = "L'heure d'ouverture est obligatoire.")
        LocalTime heure_debut,

        @NotNull(message = "L'heure de fermeture est obligatoire.")
        LocalTime heure_fin,

        @Min(value = 1, message = "La durée d'un match doit être positive.")
        int duree_match_minutes,

        @Min(value = 0, message = "La pause entre les matches ne peut pas être négative.")
        int pause_minutes
) {
}