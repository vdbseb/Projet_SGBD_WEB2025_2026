package be.angularpadelclub.DTO;

import be.angularpadelclub.Enum.MatchStatus;
import be.angularpadelclub.Enum.MatchType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record MatchDTO(
        Integer id,

        @NotNull(message = "Le terrain du match est obligatoire.")
        Integer terrainId,

        String terrainName,

        @NotNull(message = "L'organisateur du match est obligatoire.")
        Integer organisateurId,

        @NotNull(message = "La date du match est obligatoire.")
        LocalDate dateMatch,

        @NotNull(message = "L'heure de début du match est obligatoire.")
        LocalTime heureDebut,

        @NotNull(message = "L'heure de fin du match est obligatoire.")
        LocalTime heureFin,

        @NotNull(message = "Le type de match est obligatoire.")
        MatchType matchType,

        @NotNull(message = "Le statut du match est obligatoire.")
        MatchStatus statut,

        @Positive(message = "Le prix total du match doit être positif.")
        Integer prixTotal,

        List<String> playerMatricules
) {
}