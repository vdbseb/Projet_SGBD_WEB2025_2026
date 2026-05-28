package be.angularpadelclub.DTO;

import be.angularpadelclub.Enum.PaiementMethode;
import be.angularpadelclub.Enum.PaiementProvider;
import be.angularpadelclub.Enum.PaiementStatut;

import java.time.LocalDateTime;

public record PaiementDTO(
        Integer id,
        Integer reservationId,
        Integer participationId,
        Integer membreId,
        Integer montantCentimes,
        String devise,
        PaiementProvider provider,
        String providerPaymentId,
        String clientSecret,
        PaiementMethode methode,
        PaiementStatut statut,
        LocalDateTime dateCreation,
        LocalDateTime datePaiement,
        LocalDateTime dateExpiration
) {
}
