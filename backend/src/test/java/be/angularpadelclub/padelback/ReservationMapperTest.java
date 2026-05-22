package be.angularpadelclub.padelback;

import be.angularpadelclub.padelback.court.CourtEntity;
import be.angularpadelclub.padelback.member.MemberEntity;
import be.angularpadelclub.padelback.reservation.ReservationDTO;
import be.angularpadelclub.padelback.reservation.ReservationEntity;
import be.angularpadelclub.padelback.reservation.ReservationMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReservationMapperTest {

    private final ReservationMapper mapper = new ReservationMapper();

    @Test
    void shouldMapEntityToDto() {
        ReservationEntity entity = new ReservationEntity();

        CourtEntity court = new CourtEntity();
        court.setId(UUID.randomUUID());
        court.setName("Court 1");

        MemberEntity member = new MemberEntity();
        member.setId(UUID.randomUUID());
        member.setMatricule("G1234");

        entity.setCourt(court);
        entity.setMember(member);
        entity.setDate(LocalDate.of(2026, 6, 20));
        entity.setStartTime(LocalTime.of(18, 0));
        entity.setEndTime(LocalTime.of(19, 30));

        ReservationDTO dto = mapper.toDTO(entity);

        assertEquals("Court 1", dto.courtName());
        assertEquals("G1234", dto.playerMatricule());
    }
}