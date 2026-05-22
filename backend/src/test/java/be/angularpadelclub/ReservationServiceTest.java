package be.angularpadelclub;

import be.angularpadelclub.DTO.ReservationDTO;
import be.angularpadelclub.Entity.CourtEntity;
import be.angularpadelclub.Entity.MemberEntity;
import be.angularpadelclub.Entity.ReservationEntity;
import be.angularpadelclub.Mapper.ReservationMapper;
import be.angularpadelclub.Repository.CourtRepository;
import be.angularpadelclub.Repository.MemberRepository;
import be.angularpadelclub.Repository.ReservationRepository;
import be.angularpadelclub.Service.ReservationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

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
    void shouldSaveReservation() {
        Integer courtId = 1;
        String matricule = "G1234";

        CourtEntity court = new CourtEntity();
        court.setId(courtId);

        MemberEntity member = new MemberEntity();
        member.setMatricule(matricule);

        ReservationDTO dto = new ReservationDTO(
                null,
                courtId,
                null,
                matricule,
                matricule,
                LocalDate.of(2026, 6, 20),
                LocalTime.of(18, 0),
                LocalTime.of(19, 30)
        );

        ReservationEntity entity = new ReservationEntity();

        when(courtRepository.findById(courtId)).thenReturn(Optional.of(court));
        when(memberRepository.findById(matricule)).thenReturn(Optional.of(member));
        when(reservationMapper.toEntity(dto, court, member)).thenReturn(entity);

        reservationService.addReservation(dto);

        verify(reservationRepository).save(entity);
    }
}