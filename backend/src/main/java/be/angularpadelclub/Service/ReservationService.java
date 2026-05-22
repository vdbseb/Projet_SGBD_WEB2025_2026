package be.angularpadelclub.Service;

import be.angularpadelclub.DTO.ReservationDTO;
import be.angularpadelclub.Entity.CourtEntity;
import be.angularpadelclub.Entity.MemberEntity;
import be.angularpadelclub.Entity.ReservationEntity;
import be.angularpadelclub.Mapper.ReservationMapper;
import be.angularpadelclub.Repository.CourtRepository;
import be.angularpadelclub.Repository.MemberRepository;
import be.angularpadelclub.Repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final CourtRepository courtRepository;
    private final MemberRepository memberRepository;
    private final ReservationMapper reservationMapper;

    public List<ReservationEntity> findAll() {
        return reservationRepository.findAll();
    }

    public Optional<ReservationEntity> findById(UUID id) {
        return reservationRepository.findById(id);
    }

    public ReservationDTO addReservation(ReservationDTO dto) {
        CourtEntity court = courtRepository.findById(dto.courtId())
                .orElseThrow(() -> new RuntimeException("Court not found"));

        MemberEntity member = memberRepository.findById(dto.playerMatricule())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        ReservationEntity entity = reservationMapper.toEntity(dto, court, member);

        return reservationMapper.toDTO(reservationRepository.save(entity));
    }

    public void deleteReservation(UUID id) {
        reservationRepository.deleteById(id);
    }

    public List<ReservationDTO> findByCourtAndDate(Integer courtId, LocalDate date) {
        return reservationRepository.findByCourtIdAndDate(courtId, date)
                .stream()
                .map(reservationMapper::toDTO)
                .toList();
    }
}