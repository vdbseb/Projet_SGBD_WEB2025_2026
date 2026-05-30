package be.angularpadelclub.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

public record PenaliteDTO(
        Integer id,
        Integer membreId,
        Integer matchId,
        LocalDate dateDebut,
        LocalDate dateFin,
        String raison,
        boolean active,
        long joursRestants,
        LocalDate matchDate,
        LocalTime matchStartTime,
        LocalTime matchEndTime,
        String courtName,
        String siteName
) {
}