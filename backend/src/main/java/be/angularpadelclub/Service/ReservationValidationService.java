package be.angularpadelclub.Service;

import be.angularpadelclub.Entity.*;
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
        validateMemberCanReserve(member);
        validateReservationDelay(member, court, date);
        validateSiteIsOpen(court.getSite().getId(), date);
        validateSiteIsActive(court.getSite());

        HoraireSiteEntity horaire = horaireSiteService.findBySiteIdAndAnnee(
                court.getSite().getId(),
                date.getYear()
        );

        validateInsideOpeningHours(horaire, startTime);
        validateCourtAvailable(court, date, startTime, horaire);

        return horaire;
    }

    private void validateSiteIsActive(SiteEntity site) {
        if (site == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reservation impossible : le site est introuvable."
            );
        }

        if (!site.isActif()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Reservation impossible : le site est désactivé."
            );
        }
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
                    "Un match prive doit avoir exactement 4 joueurs."
            );
        }
    }

    private void validateMemberCanReserve(MembreEntity member) {
        if (!member.isActif()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Reservation impossible : membre inactif."
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
                    "Reservation impossible : le membre a une penalite active."
            );
        }

        boolean hasOpenDebt = detteMembreRepository.existsByMembre_IdAndStatut(
                member.getId(),
                DetteStatut.OUVERTE
        );

        if (hasOpenDebt) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Reservation impossible : le membre a un solde du."
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
                    "Reservation impossible : la date est dans le passe."
            );
        }

        String matricule = member.getMatricule();

        if (matricule.startsWith("G")) {
            if (date.isAfter(today.plusWeeks(3))) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Un membre global ne peut reserver que 3 semaines a l'avance."
                );
            }
            return;
        }

        if (matricule.startsWith("S")) {
            if (member.getSite() == null ||
                    !member.getSite().getId().equals(court.getSite().getId())) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Un membre de site ne peut reserver que sur son propre site."
                );
            }

            if (date.isAfter(today.plusWeeks(2))) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Un membre de site ne peut reserver que 2 semaines a l'avance."
                );
            }
            return;
        }

        if (matricule.startsWith("L")) {
            if (date.isAfter(today.plusDays(5))) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Un membre libre ne peut reserver que 5 jours a l'avance."
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
                    "Reservation impossible : le site est ferme a cette date."
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
                    "Reservation impossible : en dehors des heures d'ouverture."
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
                        "Reservation impossible : ce terrain est deja reserve sur ce creneau."
                );
            }
        }
    }
}
