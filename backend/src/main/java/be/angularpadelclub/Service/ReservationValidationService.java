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
        validateRequiredReservationData(court, member, date, startTime);
        validateMemberCanReserve(member);
        validateCourtCanBeReserved(court);
        validateSiteCanReceiveReservations(court.getSite());
        validateReservationDelay(member, court, date);
        validateSiteIsOpen(court.getSite().getId(), date);

        HoraireSiteEntity schedule = findScheduleForReservation(court, date);

        validateInsideOpeningHours(schedule, startTime);
        validateCourtAvailable(court, date, startTime, schedule);

        return schedule;
    }

    public void validateParticipants(List<String> participants) {
        List<String> normalizedParticipants =
                ClubBusinessRules.normalizeParticipantMatricules(participants);

        validateMaximumPlayerCount(normalizedParticipants);
        validatePrivateMatchIsCompleteWhenParticipantsAreProvided(
                normalizedParticipants
        );
        validateNoDuplicateParticipants(normalizedParticipants);
    }

    private void validateRequiredReservationData(
            CourtEntity court,
            MembreEntity member,
            LocalDate date,
            LocalTime startTime
    ) {
        if (court == null) {
            throw badRequest("Réservation impossible : le terrain est obligatoire.");
        }

        if (member == null) {
            throw badRequest("Réservation impossible : le membre est obligatoire.");
        }

        if (date == null) {
            throw badRequest("Réservation impossible : la date est obligatoire.");
        }

        if (startTime == null) {
            throw badRequest("Réservation impossible : l'heure de début est obligatoire.");
        }
    }

    private void validateMemberCanReserve(MembreEntity member) {
        if (!member.isActif()) {
            throw forbidden("Réservation impossible : membre inactif.");
        }

        if (hasActivePenalty(member)) {
            throw forbidden("Réservation impossible : le membre a une pénalité active.");
        }

        if (hasOpenDebt(member)) {
            throw forbidden("Réservation impossible : le membre a un solde dû.");
        }
    }

    private boolean hasActivePenalty(MembreEntity member) {
        return penaliteRepository.existsByMembre_IdAndActiveTrueAndDateFinGreaterThanEqual(
                member.getId(),
                LocalDate.now()
        );
    }

    private boolean hasOpenDebt(MembreEntity member) {
        return detteMembreRepository.existsByMembre_IdAndStatut(
                member.getId(),
                DetteStatut.OUVERTE
        );
    }

    private void validateCourtCanBeReserved(CourtEntity court) {
        if (court.getSite() == null) {
            throw badRequest("Réservation impossible : le terrain n'est lié à aucun site.");
        }

        if (!court.isActif()) {
            throw conflict("Réservation impossible : le terrain est désactivé.");
        }

        if (court.isMaintenance()) {
            throw conflict("Réservation impossible : le terrain est en maintenance.");
        }
    }

    private void validateSiteCanReceiveReservations(SiteEntity site) {
        if (site == null) {
            throw badRequest("Réservation impossible : le site est introuvable.");
        }

        if (!site.isActif()) {
            throw conflict("Réservation impossible : le site est désactivé.");
        }
    }

    private void validateReservationDelay(
            MembreEntity member,
            CourtEntity court,
            LocalDate reservationDate
    ) {
        LocalDate today = LocalDate.now();

        if (reservationDate.isBefore(today)) {
            throw conflict("Réservation impossible : la date est dans le passé.");
        }

        String matricule = member.getMatricule();

        if (ClubBusinessRules.isBlank(matricule)) {
            throw conflict("Réservation impossible : le membre n'a pas de matricule.");
        }

        if (ClubBusinessRules.isGlobalMemberMatricule(matricule)) {
            validateGlobalMemberDelay(reservationDate, today);
            return;
        }

        if (ClubBusinessRules.isSiteMemberMatricule(matricule)) {
            validateSiteMemberCanReserveOnCourt(member, court);
            validateSiteMemberDelay(reservationDate, today);
            return;
        }

        if (ClubBusinessRules.isFreeMemberMatricule(matricule)) {
            validateFreeMemberDelay(reservationDate, today);
            return;
        }

        throw conflict("Type de membre inconnu pour le matricule : " + matricule + ".");
    }

    private void validateGlobalMemberDelay(
            LocalDate reservationDate,
            LocalDate today
    ) {
        if (reservationDate.isAfter(today.plusWeeks(
                ClubBusinessRules.GLOBAL_MEMBER_RESERVATION_WEEKS
        ))) {
            throw forbidden(
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
            throw forbidden("Un membre de site doit être lié à un site.");
        }

        if (!member.getSite().getId().equals(court.getSite().getId())) {
            throw forbidden("Un membre de site ne peut réserver que sur son propre site.");
        }
    }

    private void validateSiteMemberDelay(
            LocalDate reservationDate,
            LocalDate today
    ) {
        if (reservationDate.isAfter(today.plusWeeks(
                ClubBusinessRules.SITE_MEMBER_RESERVATION_WEEKS
        ))) {
            throw forbidden(
                    "Un membre de site ne peut réserver que "
                            + ClubBusinessRules.SITE_MEMBER_RESERVATION_WEEKS
                            + " semaines à l'avance."
            );
        }
    }

    private void validateFreeMemberDelay(
            LocalDate reservationDate,
            LocalDate today
    ) {
        if (reservationDate.isAfter(today.plusDays(
                ClubBusinessRules.FREE_MEMBER_RESERVATION_DAYS
        ))) {
            throw forbidden(
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
        boolean isSiteClosed = jourFermetureService.existsBySiteAndDate(siteId, date);
        boolean isGloballyClosed = jourFermetureService.existsGlobalByDate(date);

        if (isSiteClosed || isGloballyClosed) {
            throw conflict("Réservation impossible : le site est fermé à cette date.");
        }
    }

    private HoraireSiteEntity findScheduleForReservation(
            CourtEntity court,
            LocalDate date
    ) {
        return horaireSiteService.findBySiteIdAndAnnee(
                court.getSite().getId(),
                date.getYear()
        );
    }

    private void validateInsideOpeningHours(
            HoraireSiteEntity schedule,
            LocalTime startTime
    ) {
        if (schedule == null) {
            throw conflict(
                    "Réservation impossible : aucun horaire n'est défini pour ce site et cette année."
            );
        }

        LocalTime endTime = startTime.plusMinutes(
                schedule.getDuree_match_minutes()
        );

        if (startTime.isBefore(schedule.getHeure_debut())
                || endTime.isAfter(schedule.getHeure_fin())) {
            throw conflict("Réservation impossible : en dehors des heures d'ouverture.");
        }
    }

    private void validateCourtAvailable(
            CourtEntity court,
            LocalDate date,
            LocalTime startTime,
            HoraireSiteEntity schedule
    ) {
        LocalTime endTime = startTime.plusMinutes(
                schedule.getDuree_match_minutes()
        );
        LocalTime blockedEndTime = endTime.plusMinutes(
                schedule.getPause_minutes()
        );

        List<ReservationEntity> existingReservations =
                reservationRepository.findByCourtAndDate(court, date);

        for (ReservationEntity existingReservation : existingReservations) {
            if (canIgnoreExistingReservation(existingReservation)) {
                continue;
            }

            if (overlapsReservedSlotWithPause(
                    startTime,
                    blockedEndTime,
                    existingReservation,
                    schedule
            )) {
                throw conflict(
                        "Réservation impossible : ce terrain est déjà réservé sur ce créneau."
                );
            }
        }
    }

    private boolean overlapsReservedSlotWithPause(
            LocalTime requestedStartTime,
            LocalTime requestedBlockedEndTime,
            ReservationEntity existingReservation,
            HoraireSiteEntity schedule
    ) {
        LocalTime existingBlockedEndTime = existingReservation
                .getEndTime()
                .plusMinutes(schedule.getPause_minutes());

        return requestedStartTime.isBefore(existingBlockedEndTime)
                && requestedBlockedEndTime.isAfter(
                existingReservation.getStartTime()
        );
    }

    private boolean canIgnoreExistingReservation(ReservationEntity existing) {
        return existing.getStatut() == ReservationStatus.ANNULEE
                || (
                existing.getMatch() != null
                        && existing.getMatch().getStatut() == MatchStatus.ANNULE
        );
    }

    private void validateMaximumPlayerCount(List<String> participantMatricules) {
        int totalPlayers = 1 + participantMatricules.size();

        if (totalPlayers > ClubBusinessRules.MAX_PLAYERS_PER_MATCH) {
            throw conflict(
                    "Un match ne peut pas avoir plus de "
                            + ClubBusinessRules.MAX_PLAYERS_PER_MATCH
                            + " joueurs."
            );
        }
    }

    private void validatePrivateMatchIsCompleteWhenParticipantsAreProvided(
            List<String> participantMatricules
    ) {
        int totalPlayers = 1 + participantMatricules.size();

        if (!participantMatricules.isEmpty()
                && totalPlayers != ClubBusinessRules.MAX_PLAYERS_PER_MATCH) {
            throw conflict(
                    "Un match privé doit avoir exactement "
                            + ClubBusinessRules.MAX_PLAYERS_PER_MATCH
                            + " joueurs."
            );
        }
    }

    private void validateNoDuplicateParticipants(List<String> participantMatricules) {
        long distinctParticipantCount = participantMatricules
                .stream()
                .distinct()
                .count();

        if (distinctParticipantCount != participantMatricules.size()) {
            throw conflict("Un même participant ne peut pas être ajouté plusieurs fois.");
        }
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
}