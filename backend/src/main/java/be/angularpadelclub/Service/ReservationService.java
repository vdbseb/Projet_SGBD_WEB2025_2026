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
            MatchRepository matchRepository, ParticipationRepository participationRepository

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
        return matchRepository.findAll()
                .stream()
                .map(match -> new ReservationDTO(
                        match.getId(),
                        match.getDateMatch(),
                        match.getHeureFin(),
                        match.getHeureDebut(),
                        match.getTerrain().getId(),
                        match.getTerrain().getSite().getNom(),
                        match.getOrganisateur().getId(),
                        match.getTypeMatch(),
                        match.getParticipations() == null
                                ? List.of()
                                : match.getParticipations()
                                .stream()
                                .map(p -> p.getMembre().getMatricule())
                                .toList()
                ))
                .toList();
    }

    public Optional<ReservationDTO> findById(int id) {
        return matchRepository.findById(id)
                .map(match -> new ReservationDTO(
                        match.getId(),
                        match.getDateMatch(),
                        match.getHeureFin(),
                        match.getHeureDebut(),
                        match.getTerrain().getId(),
                        match.getTerrain().getSite().getNom(),
                        match.getOrganisateur().getId(),
                        match.getTypeMatch(),
                        match.getParticipations() == null
                                ? List.of()
                                : match.getParticipations()
                                .stream()
                                .map(p -> p.getMembre().getMatricule())
                                .toList()
                ));
    }

    public List<ReservationEntity> findByCourtAndDate(int courtId, LocalDate date) {
        return reservationRepository.findByCourtIdAndDate(courtId, date);
    }

    public void deleteReservation(int id) {
        reservationRepository.deleteById(id);
    }

    @Transactional
    public void addReservation(ReservationDTO dto) {


        CourtEntity court = courtRepository.findById(dto.courtId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Terrain introuvable."
                ));


        MembreEntity member = membreRepository.findById(dto.memberId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Membre introuvable."
                ));

        // AC : vérifier statut membre
        if (!member.isActif()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Réservation impossible : membre inactif."
            );
        }
        // AC : vérifier que le membre n’a pas de dette / pénalité bloquante
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

        // AC : vérifier que le site est ouvert
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

        int currentYear = dto.date().getYear();

        HoraireSiteEntity horaire = horaireSiteRepository
                .findBySite_IdAndAnnee(
                        court.getSite().getId(),
                        currentYear
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

        LocalTime openingTime = horaire.getHeure_debut();
        LocalTime closingTime = horaire.getHeure_fin();

        // AC : vérifier que la réservation respecte les horaires du site
        if (startTime.isBefore(openingTime) || endTime.isAfter(closingTime)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Réservation impossible : en dehors des heures d'ouverture."
            );
        }

        // AC : vérifier que le terrain est disponible
        List<ReservationEntity> existingReservations =
                reservationRepository.findByCourtAndDate(
                        court,
                        dto.date()
                );

        for (ReservationEntity existing : existingReservations) {

            LocalTime existingBlockedEnd =
                    existing.getEndTime()
                            .plusMinutes(
                                    horaire.getPause_minutes()
                            );

            boolean overlap =
                    startTime.isBefore(existingBlockedEnd)
                            && blockedEndTime.isAfter(
                            existing.getStartTime()
                    );

            if (overlap) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Réservation impossible : ce terrain est déjà réservé sur ce créneau."
                );
            }
        }

        ReservationEntity reservation = reservationMapper.toEntity(dto, court, member);
        reservation.setId(null);
        reservation.setEndTime(endTime);

        reservationRepository.save(reservation);

        MatchEntity match = new MatchEntity();
        match.setTerrain(court);
        match.setOrganisateur(member);
        match.setDateMatch(dto.date());
        match.setHeureDebut(dto.startTime());
        match.setHeureFin(endTime);
        match.setPrixTotal(60);
        match.setStatut(MatchStatus.OUVERT);
        match.setCreatedAt(LocalDateTime.now());

        if (dto.participantMatricules() == null || dto.participantMatricules().isEmpty()) {
            match.setTypeMatch(MatchType.PUBLIC);
        } else {
            match.setTypeMatch(MatchType.PRIVE);
            match.setStatut(MatchStatus.COMPLET);
        }
        MatchEntity savedMatch =
                matchRepository.save(match);

        // participation organisateur
        ParticipationEntity participation = new ParticipationEntity();
        participation.setMatch(savedMatch);
        participation.setDateInscription(LocalDateTime.now());
        participation.setMembre(member);

        participationRepository.save(participation);

        // participations autres joueurs si match privé
        if (dto.participantMatricules() != null) {
            for (String matricule : dto.participantMatricules()) {
                MembreEntity joueur = membreRepository.findByMatricule(matricule)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Joueur introuvable : " + matricule
                        ));

                ParticipationEntity p = new ParticipationEntity();
                p.setMatch(savedMatch);
                p.setMembre(joueur);
                p.setDateInscription(LocalDateTime.now());

                participationRepository.save(p);
            }
        }
    }
}