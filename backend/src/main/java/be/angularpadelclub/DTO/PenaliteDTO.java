package be.angularpadelclub.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;
import java.time.LocalTime;

public record PenaliteDTO(
        Integer id,

        @NotNull(message = "Le membre pénalisé est obligatoire.")
        Integer membreId,

        Integer matchId,

        @NotNull(message = "La date de début de pénalité est obligatoire.")
        LocalDate dateDebut,

        @NotNull(message = "La date de fin de pénalité est obligatoire.")
        LocalDate dateFin,

        @NotBlank(message = "La raison de la pénalité est obligatoire.")
        String raison,

        boolean active,

        @PositiveOrZero(message = "Le nombre de jours restants doit être positif ou nul.")
        long joursRestants,

        LocalDate matchDate,

        LocalTime matchStartTime,

        LocalTime matchEndTime,

        String courtName,

        String siteName
) {
}