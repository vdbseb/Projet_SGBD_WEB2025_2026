package be.angularpadelclub.DTO;

import be.angularpadelclub.Enum.DetteRaison;
import be.angularpadelclub.Enum.DetteStatut;

import java.time.LocalDateTime;

public record DetteMembreDTO(
        Integer id,
        Integer membreId,
        Integer participationId,
        Integer reservationId,
        Integer montantCentimes,
        DetteStatut statut,
        DetteRaison raison,
        LocalDateTime dateCreation,
        LocalDateTime dateResolution
) {
}
