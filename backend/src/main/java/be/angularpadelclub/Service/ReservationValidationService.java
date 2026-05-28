package be.angularpadelclub.Service;

import be.angularpadelclub.Entity.*;
import be.angularpadelclub.Enum.MatchStatus;
import be.angularpadelclub.Enum.ReservationStatus;
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
    private final JourFermetureService jourFermetureService;
    private final HoraireSiteService horaireSiteService;

    public ReservationValidationService(
            PenaliteRepository penaliteRepository,
            ReservationRepository reservationRepository,
            JourFermetureService jourFermetureService,
            HoraireSiteService horaireSiteService
    ) {
        this.penaliteRepository = penaliteRepository;
        this.reservationRepository = reservationRepository;
        this.jourFermetureService = jourFermetureService;
        this.horaireSiteService = horaireSiteService;
    }

    public HoraireSiteEntity validateReservationPossible(
            CourtEntity court,
            MembreEntity member,
            LocalDate date,
            LocalTime startTime
    ) {
        validateMemberCanReserve(member);
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
        int nombreJoueurs = 1 + participants.size();

        if (nombreJoueurs > 4) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Un match ne peut pas avoir plus de 4 joueurs."
            );
        }

        if (!participants.isEmpty() && nombreJoueurs != 4) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Un match privé doit avoir exactement 4 joueurs."
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

        boolean hasBlockingPenalty = penaliteRepository.existsByMembre_IdAndActiveTrue(
                member.getId()
        );

        if (hasBlockingPenalty) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Réservation impossible : le membre a une pénalité active."
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

        if (matricule.startsWith("G")) {
            if (date.isAfter(today.plusWeeks(3))) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Un membre global ne peut réserver que 3 semaines à l'avance."
                );
            }
            return;
        }

        if (matricule.startsWith("S")) {
            if (member.getSite() == null ||
                    !member.getSite().getId().equals(court.getSite().getId())) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Un membre de site ne peut réserver que sur son propre site."
                );
            }

            if (date.isAfter(today.plusWeeks(2))) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Un membre de site ne peut réserver que 2 semaines à l'avance."
                );
            }
            return;
        }

        if (matricule.startsWith("L")) {
            if (date.isAfter(today.plusDays(5))) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Un membre libre ne peut réserver que 5 jours à l'avance."
                );
            }
            return;
        }

        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Type de membre inconnu pour le matricule : " + matricule
        );
    }

    private void validateSiteIsOpen(Integer siteId, LocalDate date) {
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
        LocalTime endTime = startTime.plusMinutes(horaire.getDuree_match_minutes());

        if (startTime.isBefore(horaire.getHeure_debut()) || endTime.isAfter(horaire.getHeure_fin())) {
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
            if (
                    existing.getStatut() == ReservationStatus.ANNULEE ||
                            existing.getMatch() != null &&
                                    existing.getMatch().getStatut() == MatchStatus.ANNULE
            ) {
                continue;
            }

            LocalTime existingBlockedEnd =
                    existing.getEndTime().plusMinutes(horaire.getPause_minutes());

            boolean overlap =
                    startTime.isBefore(existingBlockedEnd) &&
                            blockedEndTime.isAfter(existing.getStartTime());

            if (overlap) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Réservation impossible : ce terrain est déjà réservé sur ce créneau."
                );
            }
        }
    }
}