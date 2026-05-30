package be.angularpadelclub.DTO;

import be.angularpadelclub.Enum.PaiementMethode;
import be.angularpadelclub.Enum.PaiementProvider;
import be.angularpadelclub.Enum.PaiementStatut;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;

public record PaiementDTO(
        Integer id,

        Integer reservationId,

        Integer participationId,

        Integer membreId,

        @NotNull(message = "Le montant du paiement est obligatoire.")
        @PositiveOrZero(message = "Le montant du paiement doit être positif ou nul.")
        Integer montantCentimes,

        @NotBlank(message = "La devise du paiement est obligatoire.")
        String devise,

        @NotNull(message = "Le provider de paiement est obligatoire.")
        PaiementProvider provider,

        String providerPaymentId,

        String clientSecret,

        @NotNull(message = "La méthode de paiement est obligatoire.")
        PaiementMethode methode,

        @NotNull(message = "Le statut du paiement est obligatoire.")
        PaiementStatut statut,

        LocalDateTime dateCreation,

        LocalDateTime datePaiement,

        LocalDateTime dateExpiration
) {
}