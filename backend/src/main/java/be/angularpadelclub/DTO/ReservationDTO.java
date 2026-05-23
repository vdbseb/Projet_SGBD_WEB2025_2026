package be.angularpadelclub.DTO;

import java.time.LocalDate;
import java.time.LocalTime;


public record ReservationDTO(
        Integer id,
        LocalDate date,
        LocalTime endTime,
        LocalTime startTime,
        Integer courtId,
        Integer memberId
) {}