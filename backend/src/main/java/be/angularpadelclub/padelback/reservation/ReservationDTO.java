package be.angularpadelclub.padelback.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ReservationDTO(
        UUID id,
        String courtName,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        String playerMatricule
) {}
