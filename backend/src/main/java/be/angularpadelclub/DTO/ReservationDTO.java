package be.angularpadelclub.DTO;

import java.time.LocalDate;
import java.time.LocalTime;


public record ReservationDTO(
        Integer id,
        Integer courtId,
        String courtName,
        Integer memberId,
        String playerMatricule,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime
) {}