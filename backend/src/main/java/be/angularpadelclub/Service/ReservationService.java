package be.angularpadelclub.Service;

import be.angularpadelclub.DTO.ReservationDTO;
import be.angularpadelclub.Entity.CourtEntity;
import be.angularpadelclub.Entity.HoraireSiteEntity;
import be.angularpadelclub.Entity.MatchEntity;
import be.angularpadelclub.Entity.MembreEntity;
import be.angularpadelclub.Entity.ParticipationEntity;
import be.angularpadelclub.Entity.ReservationEntity;
import be.angularpadelclub.Enum.MatchStatus;
import be.angularpadelclub.Enum.MatchType;
import be.angularpadelclub.Enum.ParticipationStatut;
import be.angularpadelclub.Enum.ReservationStatus;
import be.angularpadelclub.Mapper.ReservationMapper;
import be.angularpadelclub.Repository.CourtRepository;
import be.angularpadelclub.Repository.MatchRepository;
import be.angularpadelclub.Repository.MembreRepository;
import be.angularpadelclub.Repository.ParticipationRepository;
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

    private static final int PLAYER_SHARE_CENTS = 1500;
    private static final int MATCH_PRICE_EUROS = 60;

    private final ReservationRepository reservationRepository;
    private final CourtRepository courtRepository;
    private final MembreRepository membreRepository;
    private final ReservationMapper reservationMapper;
    private final MatchRepository matchRepository;
    private final ParticipationRepository participationRepository;
    private final ReservationValidationService reservationValidationService;
    private final ReservationCancellationService reservationCancellationService;

    public ReservationService(
            ReservationRepository reservationRepository,
            CourtRepository courtRepository,
            MembreRepository membreRepository,
            ReservationMapper reservationMapper,
            MatchRepository matchRepository,
            ParticipationRepository participationRepository,
            ReservationValidationService reservationValidationService,
            ReservationCancellationService reservationCancellationService
    ) {
        this.reservationRepository = reservationRepository;
        this.courtRepository = courtRepository;
        this.membreRepository = membreRepository;
        this.reservationMapper = reservationMapper;
        this.matchRepository = matchRepository;
        this.participationRepository = participationRepository;
        this.reservationValidationService = reservationValidationService;
        this.reservationCancellationService = reservationCancellationService;
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
        return reservationRepository.findByCourtIdAndDate(
                courtId,
                date
        );
    }

    public void deleteReservation(int id) {
        reservationRepository.deleteById(id);
    }

    @Transactional
    public void addReservation(ReservationDTO dto) {

        CourtEntity court = courtRepository.findById(dto.courtId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Terrain introuvable avec l'id " + dto.courtId()
                ));

        MembreEntity member = membreRepository.findById(dto.memberId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Membre introuvable avec l'id " + dto.memberId()
                ));

        List<String> participants = dto.participantMatricules() == null
                ? List.of()
                : dto.participantMatricules();

        HoraireSiteEntity horaire =
                reservationValidationService.validateReservationPossible(
                        court,
                        member,
                        dto.date(),
                        dto.startTime()
                );

        reservationValidationService.validateParticipants(participants);

        LocalTime startTime = dto.startTime();

        LocalTime endTime = startTime.plusMinutes(
                horaire.getDuree_match_minutes()
        );

        MatchType matchType = participants.isEmpty()
                ? MatchType.PUBLIC
                : MatchType.PRIVE;

        ReservationEntity reservation = reservationMapper.toEntity(
                dto,
                court,
                member
        );

        reservation.setId(null);
        reservation.setEndTime(endTime);

        ReservationEntity savedReservation =
                reservationRepository.save(reservation);

        MatchEntity match = new MatchEntity();

        match.setTerrain(court);
        match.setOrganisateur(member);
        match.setDateMatch(dto.date());
        match.setHeureDebut(startTime);
        match.setHeureFin(endTime);
        match.setPrixTotal(MATCH_PRICE_EUROS);
        match.setCreatedAt(LocalDateTime.now());
        match.setTypeMatch(matchType);
        match.setStatut(
                matchType == MatchType.PRIVE
                        ? MatchStatus.COMPLET
                        : MatchStatus.OUVERT
        );

        match.setReservation(savedReservation);

        MatchEntity savedMatch = matchRepository.save(match);

        createParticipation(savedMatch, member);

        for (String matricule : participants) {
            MembreEntity joueur = membreRepository.findByMatricule(matricule)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Joueur introuvable : " + matricule
                    ));

            createParticipation(savedMatch, joueur);
        }
    }

    @Transactional
    public void cancelReservation(
            int id,
            int requestingMemberId
    ) {
        ReservationEntity reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Réservation introuvable avec l'id " + id
                ));

        validateManualCancellationAllowed(reservation);
        validateRequesterCanCancelWholeReservation(reservation, requestingMemberId);

        reservationCancellationService.cancelReservationByMember(reservation);
    }

    @Transactional
    public void cancelReservationByAdmin(int id) {
        ReservationEntity reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Réservation introuvable avec l'id " + id
                ));

        validateManualCancellationAllowed(reservation);

        reservationCancellationService.cancelReservationByMember(reservation);
    }

    private void createParticipation(
            MatchEntity match,
            MembreEntity membre
    ) {
        ParticipationEntity participation = new ParticipationEntity();

        participation.setMatch(match);
        participation.setMembre(membre);
        participation.setDateInscription(LocalDateTime.now());
        participation.setStatut(ParticipationStatut.EN_ATTENTE_PAIEMENT);
        participation.setMontantDuCentimes(PLAYER_SHARE_CENTS);
        participation.setDateLimitePaiement(
                LocalDateTime.of(
                        match.getDateMatch().minusDays(1),
                        match.getHeureDebut()
                )
        );

        participationRepository.save(participation);
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