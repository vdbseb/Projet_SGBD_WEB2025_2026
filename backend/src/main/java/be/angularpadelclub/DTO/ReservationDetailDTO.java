package be.angularpadelclub.DTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ReservationDetailDTO (
    Integer id,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,

    Integer courtId,
    String courtName,

    Integer siteId,
    String siteName,

    String organizerMatricule,
    String organizerName,

    List<MemberDTO> members,

    String paiementStatut
)
{

}