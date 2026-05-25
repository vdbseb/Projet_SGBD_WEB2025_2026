package be.angularpadelclub.DTO;

import be.angularpadelclub.Enum.MatchStatus;
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
        String siteName,
        Integer memberId,
        MatchType matchType,
        MatchStatus matchStatus,
        List<String> participantMatricules
) {}