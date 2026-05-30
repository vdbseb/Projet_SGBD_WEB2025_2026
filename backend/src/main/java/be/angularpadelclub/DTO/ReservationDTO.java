package be.angularpadelclub.DTO;

import be.angularpadelclub.Enum.MatchStatus;
import be.angularpadelclub.Enum.MatchType;
import be.angularpadelclub.Enum.ReservationStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ReservationDTO(
        Integer id,

        Integer matchId,

        @NotNull(message = "La date de réservation est obligatoire.")
        LocalDate date,
// rework plus sympa poiur travailler dans le code : prévoir de mettre start time avant end time
        @NotNull(message = "L'heure de fin est obligatoire.")
        LocalTime endTime,

        @NotNull(message = "L'heure de début est obligatoire.")
        LocalTime startTime,

        @NotNull(message = "Le terrain est obligatoire.")
        Integer courtId,

        String siteName,

        @NotNull(message = "Le membre organisateur est obligatoire.")
        Integer memberId,

        ReservationStatus reservationStatus,

        @NotNull(message = "Le type de match est obligatoire.")
        MatchType matchType,

        MatchStatus matchStatus,

        List<String> participantMatricules,

        List<@Valid ParticipantReservationDTO> participants
) {
}