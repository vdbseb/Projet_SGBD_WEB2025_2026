package be.angularpadelclub.DTO;

public record ParticipantReservationDTO(
        Integer participationId,
        Integer membreId,
        String matricule,
        String prenom,
        String nom,
        String statut,
        Integer montantDuCentimes
) {
}