package be.angularpadelclub.DTO;

import be.angularpadelclub.Enum.MatchType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


public record ReservationDTO(
        Integer id,
        LocalDate date,
        LocalTime endTime,
        LocalTime startTime,
        Integer courtId,
        Integer memberId,
        MatchType matchType,
        List<String> participantMatricules
) {}