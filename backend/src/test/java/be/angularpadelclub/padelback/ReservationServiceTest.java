package be.angularpadelclub.padelback;

import be.angularpadelclub.padelback.court.CourtEntity;
import be.angularpadelclub.padelback.court.CourtRepository;
import be.angularpadelclub.padelback.member.MemberEntity;
import be.angularpadelclub.padelback.member.MemberRepository;
import be.angularpadelclub.padelback.reservation.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private CourtRepository courtRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ReservationMapper reservationMapper;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void shouldSaveReservationWhenNoConflict() {
        UUID courtId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        CourtEntity court = new CourtEntity();
        court.setId(courtId);

        MemberEntity member = new MemberEntity();
        member.setId(memberId);
        member.setMatricule("G1234");

        ReservationDTO dto = new ReservationDTO(
                null,
                courtId,
                null,
                memberId,
                null,
                LocalDate.of(2026, 6, 20),
                LocalTime.of(18, 0),
                LocalTime.of(19, 30)
        );

        ReservationEntity entity = new ReservationEntity();

        when(courtRepository.findById(courtId)).thenReturn(Optional.of(court));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(reservationRepository.existsByCourtAndDateAndStartTimeLessThanAndEndTimeGreaterThan(
                court,
                dto.date(),
                dto.endTime(),
                dto.startTime()
        )).thenReturn(false);
        when(reservationMapper.toEntity(dto, court, member)).thenReturn(entity);

        reservationService.addReservation(dto);

        verify(reservationRepository).save(entity);
    }
}