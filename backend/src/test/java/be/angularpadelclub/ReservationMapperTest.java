package be.angularpadelclub;

import be.angularpadelclub.DTO.ReservationDTO;
import be.angularpadelclub.Entity.CourtEntity;
import be.angularpadelclub.Entity.MemberEntity;
import be.angularpadelclub.Entity.ReservationEntity;
import be.angularpadelclub.Mapper.ReservationMapper;
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
        court.setId(1);
        court.setName("Court 1");

        MemberEntity member = new MemberEntity();
        member.setMatricule("G1234");

        entity.setId(UUID.randomUUID());
        entity.setCourt(court);
        entity.setMember(member);
        entity.setDate(LocalDate.of(2026, 6, 20));
        entity.setStartTime(LocalTime.of(18, 0));
        entity.setEndTime(LocalTime.of(19, 30));

        ReservationDTO dto = mapper.toDTO(entity);

        assertEquals(1, dto.courtId());
        assertEquals("Court 1", dto.courtName());
        assertEquals("G1234", dto.playerMatricule());
    }
}