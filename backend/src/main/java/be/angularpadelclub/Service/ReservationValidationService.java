package be.angularpadelclub.Service;

import be.angularpadelclub.Entity.CourtEntity;
import be.angularpadelclub.Entity.HoraireSiteEntity;
import be.angularpadelclub.Entity.MembreEntity;
import be.angularpadelclub.Entity.ReservationEntity;
import be.angularpadelclub.Entity.SiteEntity;
import be.angularpadelclub.Enum.DetteStatut;
import be.angularpadelclub.Enum.MatchStatus;
import be.angularpadelclub.Enum.ReservationStatus;
import be.angularpadelclub.Repository.DetteMembreRepository;
import be.angularpadelclub.Repository.PenaliteRepository;
import be.angularpadelclub.Repository.ReservationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReservationValidationService {

    private final PenaliteRepository penaliteRepository;
    private final ReservationRepository reservationRepository;
    private final DetteMembreRepository detteMembreRepository;
    private final JourFermetureService jourFermetureService;
    private final HoraireSiteService horaireSiteService;

    public ReservationValidationService(
            PenaliteRepository penaliteRepository,
            ReservationRepository reservationRepository,
            DetteMembreRepository detteMembreRepository,
            JourFermetureService jourFermetureService,
            HoraireSiteService horaireSiteService
    ) {
        this.penaliteRepository = penaliteRepository;
        this.reservationRepository = reservationRepository;
        this.detteMembreRepository = detteMembreRepository;
        this.jourFermetureService = jourFermetureService;
        this.horaireSiteService = horaireSiteService;
    }

    public HoraireSiteEntity validateReservationPossible(
            CourtEntity court,
            MembreEntity member,
            LocalDate date,
            LocalTime startTime
    ) {
        validateInput(court, member, date, startTime);
        validateMemberCanReserve(member);
        validateCourtCanBeReserved(court);
        validateSiteIsActive(court.getSite());
        validateReservationDelay(member, court, date);
        validateSiteIsOpen(court.getSite().getId(), date);

        HoraireSiteEntity horaire = horaireSiteService.findBySiteIdAndAnnee(
                court.getSite().getId(),
                date.getYear()
        );

        validateInsideOpeningHours(horaire, startTime);
        validateCourtAvailable(court, date, startTime, horaire);

        return horaire;
    }

    public void validateParticipants(List<String> participants) {
        List<String> safeParticipants = participants == null
                ? List.of()
                : participants.stream()
                .filter(matricule -> matricule != null && !matricule.isBlank())
                .map(matricule -> matricule.trim().toUpperCase())
                .toList();

        int nombreJoueurs = 1 + safeParticipants.size();

        if (nombreJoueurs > ClubBusinessRules.MAX_PLAYERS_PER_MATCH) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Un match ne peut pas avoir plus de "
                            + ClubBusinessRules.MAX_PLAYERS_PER_MATCH
                            + " joueurs."
            );
        }

        if (!safeParticipants.isEmpty()
                && nombreJoueurs != ClubBusinessRules.MAX_PLAYERS_PER_MATCH) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Un match privé doit avoir exactement "
                            + ClubBusinessRules.MAX_PLAYERS_PER_MATCH
                            + " joueurs."
            );
        }

        long nombreParticipantsDistincts = safeParticipants
                .stream()
                .distinct()
                .count();

        if (nombreParticipantsDistincts != safeParticipants.size()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Un même participant ne peut pas être ajouté plusieurs fois."
            );
        }
    }

    private void validateInput(
            CourtEntity court,
            MembreEntity member,
            LocalDate date,
            LocalTime startTime
    ) {
        if (court == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Réservation impossible : le terrain est obligatoire."
            );
        }

        if (member == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Réservation impossible : le membre est obligatoire."
            );
        }

        if (date == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Réservation impossible : la date est obligatoire."
            );
        }

        if (startTime == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Réservation impossible : l'heure de début est obligatoire."
            );
        }
    }

    private void validateMemberCanReserve(MembreEntity member) {
        if (!member.isActif()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Réservation impossible : membre inactif."
            );
        }

        boolean hasBlockingPenalty =
                penaliteRepository.existsByMembre_IdAndActiveTrueAndDateFinGreaterThanEqual(
                        member.getId(),
                        LocalDate.now()
                );

        if (hasBlockingPenalty) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Réservation impossible : le membre a une pénalité active."
            );
        }

        boolean hasOpenDebt = detteMembreRepository.existsByMembre_IdAndStatut(
                member.getId(),
                DetteStatut.OUVERTE
        );

        if (hasOpenDebt) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Réservation impossible : le membre a un solde dû."
            );
        }
    }

    private void validateCourtCanBeReserved(CourtEntity court) {
        if (!court.isActif()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Réservation impossible : le terrain est désactivé."
            );
        }

        if (court.isMaintenance()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Réservation impossible : le terrain est en maintenance."
            );
        }

        if (court.getSite() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Réservation impossible : le terrain n'est lié à aucun site."
            );
        }
    }

    private void validateSiteIsActive(SiteEntity site) {
        if (site == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Réservation impossible : le site est introuvable."
            );
        }

        if (!site.isActif()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Réservation impossible : le site est désactivé."
            );
        }
    }

    private void validateReservationDelay(
            MembreEntity member,
            CourtEntity court,
            LocalDate date
    ) {
        LocalDate today = LocalDate.now();

        if (date.isBefore(today)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Réservation impossible : la date est dans le passé."
            );
        }

        String matricule = member.getMatricule();

        if (matricule == null || matricule.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Réservation impossible : le membre n'a pas de matricule."
            );
        }

        String normalizedMatricule = matricule.trim().toUpperCase();

        if (normalizedMatricule.startsWith("G")) {
            validateGlobalMemberDelay(date, today);
            return;
        }

        if (normalizedMatricule.startsWith("S")) {
            validateSiteMemberCanReserveOnCourt(member, court);
            validateSiteMemberDelay(date, today);
            return;
        }

        if (normalizedMatricule.startsWith("L")) {
            validateFreeMemberDelay(date, today);
            return;
        }

        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Type de membre inconnu pour le matricule : " + matricule + "."
        );
    }

    private void validateGlobalMemberDelay(
            LocalDate date,
            LocalDate today
    ) {
        if (date.isAfter(today.plusWeeks(
                ClubBusinessRules.GLOBAL_MEMBER_RESERVATION_WEEKS
        ))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Un membre global ne peut réserver que "
                            + ClubBusinessRules.GLOBAL_MEMBER_RESERVATION_WEEKS
                            + " semaines à l'avance."
            );
        }
    }

    private void validateSiteMemberCanReserveOnCourt(
            MembreEntity member,
            CourtEntity court
    ) {
        if (member.getSite() == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Un membre de site doit être lié à un site."
            );
        }

        if (!member.getSite().getId().equals(court.getSite().getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Un membre de site ne peut réserver que sur son propre site."
            );
        }
    }

    private void validateSiteMemberDelay(
            LocalDate date,
            LocalDate today
    ) {
        if (date.isAfter(today.plusWeeks(
                ClubBusinessRules.SITE_MEMBER_RESERVATION_WEEKS
        ))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Un membre de site ne peut réserver que "
                            + ClubBusinessRules.SITE_MEMBER_RESERVATION_WEEKS
                            + " semaines à l'avance."
            );
        }
    }

    private void validateFreeMemberDelay(
            LocalDate date,
            LocalDate today
    ) {
        if (date.isAfter(today.plusDays(
                ClubBusinessRules.FREE_MEMBER_RESERVATION_DAYS
        ))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Un membre libre ne peut réserver que "
                            + ClubBusinessRules.FREE_MEMBER_RESERVATION_DAYS
                            + " jours à l'avance."
            );
        }
    }

    private void validateSiteIsOpen(
            Integer siteId,
            LocalDate date
    ) {
        boolean fermetureSite = jourFermetureService.existsBySiteAndDate(siteId, date);
        boolean fermetureGlobale = jourFermetureService.existsGlobalByDate(date);

        if (fermetureSite || fermetureGlobale) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Réservation impossible : le site est fermé à cette date."
            );
        }
    }

    private void validateInsideOpeningHours(
            HoraireSiteEntity horaire,
            LocalTime startTime
    ) {
        if (horaire == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Réservation impossible : aucun horaire n'est défini pour ce site et cette année."
            );
        }

        LocalTime endTime = startTime.plusMinutes(horaire.getDuree_match_minutes());

        if (startTime.isBefore(horaire.getHeure_debut())
                || endTime.isAfter(horaire.getHeure_fin())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Réservation impossible : en dehors des heures d'ouverture."
            );
        }
    }

    private void validateCourtAvailable(
            CourtEntity court,
            LocalDate date,
            LocalTime startTime,
            HoraireSiteEntity horaire
    ) {
        LocalTime endTime = startTime.plusMinutes(horaire.getDuree_match_minutes());
        LocalTime blockedEndTime = endTime.plusMinutes(horaire.getPause_minutes());

        List<ReservationEntity> existingReservations =
                reservationRepository.findByCourtAndDate(court, date);

        for (ReservationEntity existing : existingReservations) {
            if (canIgnoreExistingReservation(existing)) {
                continue;
            }

            LocalTime existingBlockedEnd =
                    existing.getEndTime().plusMinutes(horaire.getPause_minutes());

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
    }

    private boolean canIgnoreExistingReservation(ReservationEntity existing) {
        return existing.getStatut() == ReservationStatus.ANNULEE
                || (
                existing.getMatch() != null
                        && existing.getMatch().getStatut() == MatchStatus.ANNULE
        );
    }
}