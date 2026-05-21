package be.angularpadelclub.padelback.reservation;

import be.angularpadelclub.padelback.court.CourtEntity;
import be.angularpadelclub.padelback.court.CourtRepository;
import be.angularpadelclub.padelback.member.MemberEntity;
import be.angularpadelclub.padelback.member.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final CourtRepository courtRepository;
    private final MemberRepository memberRepository;
    private final ReservationMapper reservationMapper;

    public ReservationService(
            ReservationRepository reservationRepository,
            CourtRepository courtRepository,
            MemberRepository memberRepository,
            ReservationMapper reservationMapper
    ) {
        this.reservationRepository = reservationRepository;
        this.courtRepository = courtRepository;
        this.memberRepository = memberRepository;
        this.reservationMapper = reservationMapper;
    }

    public List<ReservationEntity> findAll() {
        return reservationRepository.findAll();
    }

    public Optional<ReservationEntity> findById(UUID id) {
        return reservationRepository.findById(id);
    }

    public List<ReservationEntity> findByCourtAndDate(UUID courtId, LocalDate date) {
        return reservationRepository.findByCourtIdAndDate(courtId, date);
    }

    public void addReservation(ReservationDTO dto) {
        CourtEntity court = courtRepository.findById(dto.courtId())
                .orElseThrow(() -> new RuntimeException("Court not found"));

        MemberEntity member = memberRepository.findById(dto.memberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        LocalTime startTime = dto.startTime();
        LocalTime endTime = startTime.plusMinutes(90);
        LocalTime blockedEndTime = endTime.plusMinutes(15);

        LocalTime openingTime = court.getSite().getOpeningTime();
        LocalTime closingTime = court.getSite().getClosingTime();

        if (startTime.isBefore(openingTime) || endTime.isAfter(closingTime)) {
            throw new RuntimeException("Réservation en dehors des heures d'ouverture.");
        }

        List<ReservationEntity> existingReservations =
                reservationRepository.findByCourtAndDate(court, dto.date());

        for (ReservationEntity existing : existingReservations) {
            LocalTime existingBlockedEnd = existing.getEndTime().plusMinutes(15);

            boolean overlap =
                    startTime.isBefore(existingBlockedEnd) &&
                            blockedEndTime.isAfter(existing.getStartTime());

            if (overlap) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Ce terrain est déjà réservé sur ce créneau."
                );
            }
        }

        ReservationEntity reservation = reservationMapper.toEntity(dto, court, member);
        reservation.setId(null);
        reservation.setEndTime(endTime);

        reservationRepository.save(reservation);
    }

    public void deleteReservation(UUID id) {
        reservationRepository.deleteById(id);
    }
}