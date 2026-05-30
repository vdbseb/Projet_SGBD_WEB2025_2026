package be.angularpadelclub.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ParticipantReservationDTO(
        Integer participationId,

        @NotNull(message = "Le membre participant est obligatoire.")
        Integer membreId,

        @NotBlank(message = "Le matricule du participant est obligatoire.")
        String matricule,

        @NotBlank(message = "Le prénom du participant est obligatoire.")
        String prenom,

        @NotBlank(message = "Le nom du participant est obligatoire.")
        String nom,

        @NotBlank(message = "Le statut du participant est obligatoire.")
        String statut,

        @PositiveOrZero(message = "Le montant dû par le participant doit être positif ou nul.")
        Integer montantDuCentimes
) {
}