package be.angularpadelclub.Service;

import be.angularpadelclub.DTO.ReservationDTO;
import be.angularpadelclub.Entity.CourtEntity;
import be.angularpadelclub.Entity.HoraireSiteEntity;
import be.angularpadelclub.Entity.MatchEntity;
import be.angularpadelclub.Entity.MembreEntity;
import be.angularpadelclub.Entity.ReservationEntity;
import be.angularpadelclub.Enum.MatchType;
import be.angularpadelclub.Enum.ReservationStatus;
import be.angularpadelclub.Mapper.ReservationMapper;
import be.angularpadelclub.Repository.MatchRepository;
import be.angularpadelclub.Repository.ReservationRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final MatchRepository matchRepository;
    private final ReservationValidationService reservationValidationService;
    private final ReservationCancellationService reservationCancellationService;
    private final ReferenceLookupService referenceLookupService;
    private final ParticipationService participationService;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationMapper reservationMapper,
            MatchRepository matchRepository,
            ReservationValidationService reservationValidationService,
            ReservationCancellationService reservationCancellationService,
            ReferenceLookupService referenceLookupService,
            ParticipationService participationService
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
        this.matchRepository = matchRepository;
        this.reservationValidationService = reservationValidationService;
        this.reservationCancellationService = reservationCancellationService;
        this.referenceLookupService = referenceLookupService;
        this.participationService = participationService;
    }

    public List<ReservationDTO> findAll() {
        return reservationRepository.findAll()
                .stream()
                .map(reservationMapper::toDTO)
                .toList();
    }

    public Optional<ReservationDTO> findById(int id) {
        return reservationRepository.findById(id)
                .map(reservationMapper::toDTO);
    }

    public List<ReservationEntity> findByCourtAndDate(
            int courtId,
            LocalDate date
    ) {
        referenceLookupService.findCourtOrThrow(courtId);

        if (date == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La date de réservation est obligatoire."
            );
        }

        return reservationRepository.findByCourtIdAndDate(
                courtId,
                date
        );
    }

    @Transactional
    public void deleteReservation(int id) {
        ReservationEntity reservation =
                referenceLookupService.findReservationOrThrow(id);

        validateManualCancellationAllowed(reservation);

        reservationCancellationService.cancelReservationForClubReason(
                reservation
        );
    }

    @Transactional
    public void addReservation(ReservationDTO dto) {
        validateReservationCreateRequest(dto);

        CourtEntity court = referenceLookupService.findCourtOrThrow(
                dto.courtId()
        );

        MembreEntity member = referenceLookupService.findMembreOrThrow(
                dto.memberId()
        );

        List<String> participantMatricules = normalizeParticipants(
                dto.participantMatricules()
        );

        validateOrganizerIsNotInParticipants(
                member,
                participantMatricules
        );

        HoraireSiteEntity horaire =
                reservationValidationService.validateReservationPossible(
                        court,
                        member,
                        dto.date(),
                        dto.startTime()
                );

        reservationValidationService.validateParticipants(
                participantMatricules
        );

        LocalTime startTime = dto.startTime();
        LocalTime endTime = startTime.plusMinutes(
                horaire.getDuree_match_minutes()
        );

        MatchType matchType = participantMatricules.isEmpty()
                ? MatchType.PUBLIC
                : MatchType.PRIVE;

        ReservationEntity reservation = reservationMapper.toEntity(
                dto,
                court,
                member
        );

        reservation.setId(null);
        reservation.setEndTime(endTime);
        reservation.setStatut(ReservationStatus.EN_ATTENTE_PAIEMENT);

        ReservationEntity savedReservation =
                reservationRepository.save(reservation);

        MatchEntity savedMatch = createMatchForReservation(
                savedReservation,
                court,
                member,
                dto.date(),
                startTime,
                endTime,
                matchType,
                participantMatricules.size() + 1
        );

        participationService.createPendingParticipation(savedMatch, member);

        for (String matricule : participantMatricules) {
            MembreEntity joueur =
                    referenceLookupService.findMembreByMatriculeOrThrow(
                            matricule
                    );

            participationService.createPendingParticipation(savedMatch, joueur);
        }
    }

    @Transactional
    public void cancelReservation(
            int id,
            int requestingMemberId
    ) {
        ReservationEntity reservation =
                referenceLookupService.findReservationOrThrow(id);

        referenceLookupService.findMembreOrThrow(requestingMemberId);

        validateManualCancellationAllowed(reservation);
        validateRequesterCanCancelWholeReservation(
                reservation,
                requestingMemberId
        );

        reservationCancellationService.cancelReservationByMember(reservation);
    }

    @Transactional
    public void cancelReservationByAdmin(int id) {
        ReservationEntity reservation =
                referenceLookupService.findReservationOrThrow(id);

        validateManualCancellationAllowed(reservation);

        reservationCancellationService.cancelReservationForClubReason(
                reservation
        );
    }

    private void validateReservationCreateRequest(ReservationDTO dto) {
        if (dto == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Les données de réservation sont obligatoires."
            );
        }

        if (dto.courtId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le terrain est obligatoire pour créer une réservation."
            );
        }

        if (dto.memberId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le membre organisateur est obligatoire pour créer une réservation."
            );
        }

        if (dto.date() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La date de réservation est obligatoire."
            );
        }

        if (dto.startTime() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "L'heure de début est obligatoire."
            );
        }
    }

    private List<String> normalizeParticipants(
            List<String> participantMatricules
    ) {
        if (participantMatricules == null) {
            return List.of();
        }

        return participantMatricules.stream()
                .filter(matricule -> matricule != null && !matricule.isBlank())
                .map(matricule -> matricule.trim().toUpperCase())
                .distinct()
                .toList();
    }

    private void validateOrganizerIsNotInParticipants(
            MembreEntity organizer,
            List<String> participantMatricules
    ) {
        if (organizer == null || organizer.getMatricule() == null) {
            return;
        }

        boolean organizerAlsoParticipant = participantMatricules.contains(
                organizer.getMatricule().trim().toUpperCase()
        );

        if (organizerAlsoParticipant) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "L'organisateur ne doit pas être ajouté une seconde fois comme participant."
            );
        }
    }

    private MatchEntity createMatchForReservation(
            ReservationEntity reservation,
            CourtEntity court,
            MembreEntity organizer,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            MatchType matchType,
            int totalPlayers
    ) {
        MatchEntity match = new MatchEntity();

        match.setTerrain(court);
        match.setOrganisateur(organizer);
        match.setDateMatch(date);
        match.setHeureDebut(startTime);
        match.setHeureFin(endTime);
        match.setPrixTotal(ClubBusinessRules.DEFAULT_MATCH_PRICE_EUROS);
        match.setCreatedAt(LocalDateTime.now());
        match.setTypeMatch(matchType);
        match.setStatut(ClubBusinessRules.resolveMatchStatusAfterParticipantCount(
                matchType,
                totalPlayers
        ));
        match.setReservation(reservation);

        return matchRepository.save(match);
    }

    private void validateManualCancellationAllowed(
            ReservationEntity reservation
    ) {
        if (reservation.getStatut() == ReservationStatus.ANNULEE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Réservation déjà annulée."
            );
        }

        if (reservation.getStatut() == ReservationStatus.TERMINEE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Impossible d'annuler une réservation terminée."
            );
        }
    }

    private void validateRequesterCanCancelWholeReservation(
            ReservationEntity reservation,
            int requestingMemberId
    ) {
        Integer reservationOwnerId = reservation.getMember() != null
                ? reservation.getMember().getId()
                : null;

        Integer matchOrganizerId = reservation.getMatch() != null
                && reservation.getMatch().getOrganisateur() != null
                ? reservation.getMatch().getOrganisateur().getId()
                : null;

        boolean isReservationOwner =
                reservationOwnerId != null
                        && reservationOwnerId.equals(requestingMemberId);

        boolean isMatchOrganizer =
                matchOrganizerId != null
                        && matchOrganizerId.equals(requestingMemberId);

        if (isReservationOwner || isMatchOrganizer) {
            return;
        }

        if (reservation.getMatch() != null
                && reservation.getMatch().getTypeMatch() == MatchType.PUBLIC) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Seul l'organisateur peut annuler le match complet. Un participant doit quitter le match public."
            );
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Seul l'organisateur peut annuler cette réservation."
        );
    }
}