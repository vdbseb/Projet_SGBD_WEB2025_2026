package be.angularpadelclub.Service;

import be.angularpadelclub.Entity.MatchEntity;
import be.angularpadelclub.Entity.ParticipationEntity;
import be.angularpadelclub.Entity.PenaliteEntity;
import be.angularpadelclub.Enum.DetteRaison;
import be.angularpadelclub.Enum.MatchStatus;
import be.angularpadelclub.Enum.MatchType;
import be.angularpadelclub.Enum.ParticipationStatut;
import be.angularpadelclub.Repository.MatchRepository;
import be.angularpadelclub.Repository.ParticipationRepository;
import be.angularpadelclub.Repository.PenaliteRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MatchSchedulerService {

    private static final int NOMBRE_JOUEURS_REQUIS = 4;
    private static final int DUREE_PENALITE_JOURS = 7;

    private final MatchRepository matchRepository;
    private final ParticipationRepository participationRepository;
    private final PenaliteRepository penaliteRepository;
    private final PaiementService paiementService;

    public MatchSchedulerService(
            MatchRepository matchRepository,
            ParticipationRepository participationRepository,
            PenaliteRepository penaliteRepository,
            PaiementService paiementService
    ) {
        this.matchRepository = matchRepository;
        this.participationRepository = participationRepository;
        this.penaliteRepository = penaliteRepository;
        this.paiementService = paiementService;
    }

    @Scheduled(fixedRate = 300000)
    @Transactional
    public void convertirMatchsPrivesIncompletsEnPublics() {

        LocalDateTime limite = LocalDateTime.now().plusHours(24);

        List<MatchEntity> matchsPrivesPlanifies =
                matchRepository.findByTypeMatchAndStatut(
                        MatchType.PRIVE,
                        MatchStatus.PLANIFIE
                );

        for (MatchEntity match : matchsPrivesPlanifies) {

            LocalDateTime dateHeureMatch = getDateHeureMatch(match);

            if (dateHeureMatch == null) {
                continue;
            }

            if (dateHeureMatch.isAfter(limite)) {
                continue;
            }

            long nombreJoueursActifs = compterJoueursActifs(match);

            if (nombreJoueursActifs >= NOMBRE_JOUEURS_REQUIS) {
                continue;
            }

            if (!peutEtreRenduPublic(match)) {
                continue;
            }

            convertirEnMatchPublic(match);

            creerPenaliteOrganisateur(
                    match,
                    "Match privé incomplet la veille : "
                            + nombreJoueursActifs
                            + "/"
                            + NOMBRE_JOUEURS_REQUIS
                            + " joueurs actifs."
            );

            System.out.println(
                    "Match privé incomplet converti en public : matchId="
                            + match.getId()
                            + ", joueursActifs="
                            + nombreJoueursActifs
            );
        }
    }

    @Scheduled(fixedRate = 300000)
    @Transactional
    public void libererPlacesNonPayees() {

        List<ParticipationEntity> participationsEnRetard =
                participationRepository.findByStatutAndDateLimitePaiementBefore(
                        ParticipationStatut.EN_ATTENTE_PAIEMENT,
                        LocalDateTime.now()
                );

        for (ParticipationEntity participation : participationsEnRetard) {

            MatchEntity match = participation.getMatch();

            if (match == null) {
                continue;
            }

            creerDettePourParticipationImpayee(participation, match);

            participation.setStatut(ParticipationStatut.LIBEREE);
            participationRepository.save(participation);

            boolean organisateur = estOrganisateur(match, participation);

            if (peutEtreRenduPublic(match)) {
                convertirEnMatchPublic(match);
            }

            if (organisateur) {
                creerPenaliteOrganisateur(
                        match,
                        "Organisateur non payé avant la date limite."
                );

                System.out.println(
                        "Organisateur non payé : participationId="
                                + participation.getId()
                                + ", matchId="
                                + match.getId()
                                + ", match passé public et pénalité créée"
                );
            } else {
                System.out.println(
                        "Participation non payée libérée : participationId="
                                + participation.getId()
                                + ", matchId="
                                + match.getId()
                                + ", match passé public"
                );
            }
        }
    }

    @Scheduled(fixedRate = 300000)
    @Transactional
    public void desactiverPenalitesExpirees() {

        List<PenaliteEntity> penalitesExpirees =
                penaliteRepository.findByActiveTrueAndDateFinLessThanEqual(
                        LocalDate.now()
                );

        for (PenaliteEntity penalite : penalitesExpirees) {
            penalite.setActive(false);
            penaliteRepository.save(penalite);

            System.out.println(
                    "Pénalité expirée désactivée : penaliteId="
                            + penalite.getId()
            );
        }
    }

    private LocalDateTime getDateHeureMatch(MatchEntity match) {

        if (match.getDateMatch() == null || match.getHeureDebut() == null) {
            return null;
        }

        return LocalDateTime.of(
                match.getDateMatch(),
                match.getHeureDebut()
        );
    }

    private long compterJoueursActifs(MatchEntity match) {

        if (match.getParticipations() == null) {
            return 0;
        }

        return match.getParticipations()
                .stream()
                .filter(this::estParticipationActive)
                .count();
    }

    private boolean estParticipationActive(ParticipationEntity participation) {
        return participation.getStatut() == ParticipationStatut.EN_ATTENTE_PAIEMENT
                || participation.getStatut() == ParticipationStatut.PAYEE;
    }

    private boolean peutEtreRenduPublic(MatchEntity match) {
        return match.getStatut() == MatchStatus.PLANIFIE
                || match.getStatut() == MatchStatus.OUVERT
                || match.getStatut() == MatchStatus.COMPLET;
    }

    private void convertirEnMatchPublic(MatchEntity match) {
        match.setTypeMatch(MatchType.PUBLIC);
        match.setStatut(MatchStatus.OUVERT);
        matchRepository.save(match);
    }

    private void creerPenaliteOrganisateur(
            MatchEntity match,
            String raison
    ) {
        if (match.getOrganisateur() == null) {
            return;
        }

        boolean aDejaUnePenaliteActive =
                penaliteRepository.existsByMembre_IdAndActiveTrueAndDateFinGreaterThanEqual(
                        match.getOrganisateur().getId(),
                        LocalDate.now()
                );

        if (aDejaUnePenaliteActive) {
            return;
        }

        LocalDate dateDebut = LocalDate.now();
        LocalDate dateFin = dateDebut.plusDays(DUREE_PENALITE_JOURS);

        PenaliteEntity penalite = new PenaliteEntity();
        penalite.setMembre(match.getOrganisateur());
        penalite.setMatch(match);
        penalite.setDateDebut(dateDebut);
        penalite.setDateFin(dateFin);
        penalite.setActive(true);
        penalite.setRaison(raison);

        penaliteRepository.save(penalite);

        System.out.println(
                "Pénalité créée : membreId="
                        + match.getOrganisateur().getId()
                        + ", matchId="
                        + match.getId()
                        + ", dateFin="
                        + dateFin
        );
    }

    private void creerDettePourParticipationImpayee(
            ParticipationEntity participation,
            MatchEntity match
    ) {
        paiementService.createDebt(
                participation.getMembre(),
                participation,
                match.getReservation(),
                participation.getMontantDuCentimes(),
                DetteRaison.PARTICIPATION_IMPAYEE
        );
    }

    private boolean estOrganisateur(
            MatchEntity match,
            ParticipationEntity participation
    ) {
        return match.getOrganisateur() != null
                && participation.getMembre() != null
                && match.getOrganisateur().getId().equals(
                participation.getMembre().getId()
        );
    }
}