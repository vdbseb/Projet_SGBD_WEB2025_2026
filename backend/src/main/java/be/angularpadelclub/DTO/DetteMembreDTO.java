package be.angularpadelclub.DTO;

import be.angularpadelclub.Enum.DetteRaison;
import be.angularpadelclub.Enum.DetteStatut;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;

public record DetteMembreDTO(
        Integer id,

        @NotNull(message = "Le membre lié à la dette est obligatoire.")
        Integer membreId,

        Integer participationId,

        Integer reservationId,

        @NotNull(message = "Le montant de la dette est obligatoire.")
        @PositiveOrZero(message = "Le montant de la dette doit être positif ou nul.")
        Integer montantCentimes,

        @NotNull(message = "Le statut de la dette est obligatoire.")
        DetteStatut statut,

        @NotNull(message = "La raison de la dette est obligatoire.")
        DetteRaison raison,

        LocalDateTime dateCreation,

        LocalDateTime dateResolution
) {
}