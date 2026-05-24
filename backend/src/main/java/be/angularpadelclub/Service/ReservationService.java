package be.angularpadelclub.Service;

import be.angularpadelclub.DTO.ReservationDTO;
import be.angularpadelclub.Entity.CourtEntity;
import be.angularpadelclub.Entity.HoraireSiteEntity;
import be.angularpadelclub.Entity.MembreEntity;
import be.angularpadelclub.Entity.ReservationEntity;
import be.angularpadelclub.Mapper.ReservationMapper;
import be.angularpadelclub.Repository.CourtRepository;
import be.angularpadelclub.Repository.HoraireSiteRepository;
import be.angularpadelclub.Repository.MembreRepository;
import be.angularpadelclub.Repository.ReservationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final CourtRepository courtRepository;
    private final MembreRepository membreRepository;
    private final HoraireSiteRepository horaireSiteRepository;
    private final ReservationMapper reservationMapper;

    public ReservationService(
            ReservationRepository reservationRepository,
            CourtRepository courtRepository,
            MembreRepository membreRepository,
            HoraireSiteRepository horaireSiteRepository,
            ReservationMapper reservationMapper
    ) {
        this.reservationRepository = reservationRepository;
        this.courtRepository = courtRepository;
        this.membreRepository = membreRepository;
        this.horaireSiteRepository = horaireSiteRepository;
        this.reservationMapper = reservationMapper;
    }
    public List<ReservationEntity> findAll() {
        return reservationRepository.findAll();
    }

    public Optional<ReservationEntity> findById(int id) {
        return reservationRepository.findById(id);
    }

    public List<ReservationEntity> findByCourtAndDate(int courtId, LocalDate date) {
        return reservationRepository.findByCourtIdAndDate(courtId, date);
    }

    public void deleteReservation(int id) {
        reservationRepository.deleteById(id);
    }

    public void addReservation(ReservationDTO dto) {
        CourtEntity court = courtRepository.findById(dto.courtId())
                .orElseThrow(() -> new RuntimeException("Court not found"));

        MembreEntity member = membreRepository.findById(dto.memberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        // AC : vérifier statut membre
        if (!member.isActif()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Réservation impossible : membre inactif."
            );
        }

        int currentYear = dto.date().getYear();

        HoraireSiteEntity horaire = horaireSiteRepository
                .findBySite_IdAndAnnee(court.getSite().getId(), currentYear)
                .orElseThrow(() -> new RuntimeException("Aucun horaire défini pour ce site et cette année"));

        LocalTime startTime = dto.startTime();
        LocalTime endTime = startTime.plusMinutes(horaire.getDuree_match_minutes());
        LocalTime blockedEndTime = endTime.plusMinutes(horaire.getPause_minutes());

        LocalTime openingTime = horaire.getHeure_debut();
        LocalTime closingTime = horaire.getHeure_fin();

        if (startTime.isBefore(openingTime) || endTime.isAfter(closingTime)) {
            throw new RuntimeException("Réservation en dehors des heures d'ouverture.");
        }

        List<ReservationEntity> existingReservations =
                reservationRepository.findByCourtAndDate(court, dto.date());

        for (ReservationEntity existing : existingReservations) {
            LocalTime existingBlockedEnd =
                    existing.getEndTime().plusMinutes(horaire.getPause_minutes());

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
}