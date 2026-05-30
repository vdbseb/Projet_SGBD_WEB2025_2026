package be.angularpadelclub.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ReservationDetailDTO(
        Integer id,

        @NotNull(message = "La date de réservation est obligatoire.")
        LocalDate date,

        @NotNull(message = "L'heure de début est obligatoire.")
        LocalTime startTime,

        @NotNull(message = "L'heure de fin est obligatoire.")
        LocalTime endTime,

        @NotNull(message = "Le terrain est obligatoire.")
        Integer courtId,

        String courtName,

        Integer siteId,

        String siteName,

        String organizerMatricule,

        String organizerName,

        List<@Valid MembreDTO> members,

        String paiementStatut
) {
}