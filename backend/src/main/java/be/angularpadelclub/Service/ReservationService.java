package be.angularpadelclub.Service;

import be.angularpadelclub.DTO.ReservationDTO;
import be.angularpadelclub.Entity.*;
import be.angularpadelclub.Enum.MatchStatus;
import be.angularpadelclub.Enum.MatchType;
import be.angularpadelclub.Mapper.ReservationMapper;
import be.angularpadelclub.Repository.*;
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
    private final CourtRepository courtRepository;
    private final MembreRepository membreRepository;
    private final HoraireSiteRepository horaireSiteRepository;
    private final JourFermetureRepository jourFermetureRepository;
    private final ReservationMapper reservationMapper;
    private final PenaliteRepository penaliteRepository;
    private final MatchRepository matchRepository;
    private final ParticipationRepository participationRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            CourtRepository courtRepository,
            MembreRepository membreRepository,
            HoraireSiteRepository horaireSiteRepository,
            JourFermetureRepository jourFermetureRepository,
            ReservationMapper reservationMapper,
            PenaliteRepository penaliteRepository,
            MatchRepository matchRepository,
            ParticipationRepository participationRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.courtRepository = courtRepository;
        this.membreRepository = membreRepository;
        this.horaireSiteRepository = horaireSiteRepository;
        this.jourFermetureRepository = jourFermetureRepository;
        this.reservationMapper = reservationMapper;
        this.penaliteRepository = penaliteRepository;
        this.matchRepository = matchRepository;
        this.participationRepository = participationRepository;
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

        if (!member.isActif()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Réservation impossible : membre inactif."
            );
        }

        boolean hasBlockingDebt =
                penaliteRepository.existsByMembre_IdAndActiveTrue(
                        member.getId()
                );

        if (hasBlockingDebt) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Réservation impossible : le membre a une pénalité active."
            );
        }

        boolean fermetureSite =
                jourFermetureRepository.existsBySiteIdAndDateFermeture(
                        court.getSite().getId(),
                        dto.date()
                );

        boolean fermetureGlobale =
                jourFermetureRepository.existsByGlobalTrueAndDateFermeture(
                        dto.date()
                );

        if (fermetureSite || fermetureGlobale) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Réservation impossible : le site est fermé à cette date."
            );
        }

        HoraireSiteEntity horaire = horaireSiteRepository
                .findBySite_IdAndAnnee(
                        court.getSite().getId(),
                        dto.date().getYear()
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Aucun horaire défini pour ce site et cette année."
                ));

        LocalTime startTime = dto.startTime();
        LocalTime endTime = startTime.plusMinutes(
                horaire.getDuree_match_minutes()
        );
        LocalTime blockedEndTime = endTime.plusMinutes(
                horaire.getPause_minutes()
        );

        if (startTime.isBefore(horaire.getHeure_debut())
                || endTime.isAfter(horaire.getHeure_fin())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Réservation impossible : en dehors des heures d'ouverture."
            );
        }

        List<ReservationEntity> existingReservations =
                reservationRepository.findByCourtAndDate(
                        court,
                        dto.date()
                );

        for (ReservationEntity existing : existingReservations) {

            if (existing.getMatch() != null
                    && existing.getMatch().getStatut() == MatchStatus.ANNULE) {
                continue;
            }

            LocalTime existingBlockedEnd =
                    existing.getEndTime().plusMinutes(
                            horaire.getPause_minutes()
                    );

            boolean overlap =
                    startTime.isBefore(existingBlockedEnd)
                            && blockedEndTime.isAfter(existing.getStartTime());

            if (overlap) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Réservation impossible : ce terrain est déjà réservé sur ce créneau."
                );
            }
        }

        List<String> participants = dto.participantMatricules() == null
                ? List.of()
                : dto.participantMatricules();

        int nombreJoueurs = 1 + participants.size();

        if (nombreJoueurs > 4) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Un match ne peut pas avoir plus de 4 joueurs."
            );
        }

        MatchType matchType = participants.isEmpty()
                ? MatchType.PUBLIC
                : MatchType.PRIVE;

        if (matchType == MatchType.PRIVE && nombreJoueurs != 4) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Un match privé doit avoir exactement 4 joueurs."
            );
        }

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
        match.setPrixTotal(60);
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

    private void createParticipation(
            MatchEntity match,
            MembreEntity membre
    ) {
        ParticipationEntity participation = new ParticipationEntity();

        participation.setMatch(match);
        participation.setMembre(membre);
        participation.setDateInscription(LocalDateTime.now());
        participation.setPaiement(null);

        participationRepository.save(participation);
    }
}