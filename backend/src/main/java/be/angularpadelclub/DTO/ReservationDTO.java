package be.angularpadelclub.DTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ReservationDTO(
        UUID id,
        Integer courtId,
        String courtName,
        String memberId,
        String playerMatricule,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime
) {}
