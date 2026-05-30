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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MatchSchedulerService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(MatchSchedulerService.class);

    private static final long SCHEDULER_FIXED_RATE_MS = 300_000L;

    private static final List<ParticipationStatut> STATUTS_PARTICIPATION_ACTIVE = List.of(
            ParticipationStatut.EN_ATTENTE_PAIEMENT,
            ParticipationStatut.PAYEE
    );

    private static final List<MatchStatus> STATUTS_MATCH_RENDABLE_PUBLIC = List.of(
            MatchStatus.PLANIFIE,
            MatchStatus.OUVERT,
            MatchStatus.COMPLET
    );

    private static final List<MatchStatus> STATUTS_MATCH_PUBLIC_FACTURABLE = List.of(
            MatchStatus.PLANIFIE,
            MatchStatus.OUVERT,
            MatchStatus.COMPLET
    );

    private final MatchRepository matchRepository;
    private final ParticipationRepository participationRepository;
    private final PenaliteRepository penaliteRepository;
    private final PaiementService paiementService;
    private final MatchBillingService matchBillingService;

    public MatchSchedulerService(
            MatchRepository matchRepository,
            ParticipationRepository participationRepository,
            PenaliteRepository penaliteRepository,
            PaiementService paiementService,
            MatchBillingService matchBillingService
    ) {
        this.matchRepository = matchRepository;
        this.participationRepository = participationRepository;
        this.penaliteRepository = penaliteRepository;
        this.paiementService = paiementService;
        this.matchBillingService = matchBillingService;
    }

    @Scheduled(fixedRate = SCHEDULER_FIXED_RATE_MS)
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

            if (dateHeureMatch == null || dateHeureMatch.isAfter(limite)) {
                continue;
            }

            long nombreJoueursActifs = compterJoueursActifs(match);

            if (nombreJoueursActifs >= ClubBusinessRules.MAX_PLAYERS_PER_MATCH) {
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
                            + ClubBusinessRules.MAX_PLAYERS_PER_MATCH
                            + " joueurs actifs."
            );

            LOGGER.info(
                    "Match privé incomplet converti en public : matchId={}, joueursActifs={}",
                    match.getId(),
                    nombreJoueursActifs
            );
        }
    }

    @Scheduled(fixedRate = SCHEDULER_FIXED_RATE_MS)
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
                LOGGER.warn(
                        "Participation en retard ignorée car aucun match associé : participationId={}",
                        participation.getId()
                );
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

                LOGGER.info(
                        "Organisateur non payé : participationId={}, matchId={}, match passé public et pénalité créée.",
                        participation.getId(),
                        match.getId()
                );
            } else {
                LOGGER.info(
                        "Participation non payée libérée : participationId={}, matchId={}, match passé public.",
                        participation.getId(),
                        match.getId()
                );
            }
        }
    }

    @Scheduled(fixedRate = SCHEDULER_FIXED_RATE_MS)
    @Transactional
    public void facturerSoldesOrganisateursMatchsPublicsIncomplets() {
        LocalDateTime maintenant = LocalDateTime.now();

        List<MatchEntity> matchsPublics =
                getMatchsPublicsPotentiellementFacturables();

        for (MatchEntity match : matchsPublics) {
            LocalDateTime dateHeureMatch = getDateHeureMatch(match);

            if (dateHeureMatch == null || dateHeureMatch.isAfter(maintenant)) {
                continue;
            }

            matchBillingService.facturerSoldeOrganisateurSiNecessaire(match);
        }
    }

    @Scheduled(fixedRate = SCHEDULER_FIXED_RATE_MS)
    @Transactional
    public void desactiverPenalitesExpirees() {
        List<PenaliteEntity> penalitesExpirees =
                penaliteRepository.findByActiveTrueAndDateFinLessThanEqual(
                        LocalDate.now()
                );

        for (PenaliteEntity penalite : penalitesExpirees) {
            penalite.setActive(false);
        }

        penaliteRepository.saveAll(penalitesExpirees);

        if (!penalitesExpirees.isEmpty()) {
            LOGGER.info(
                    "{} pénalité(s) expirée(s) désactivée(s).",
                    penalitesExpirees.size()
            );
        }
    }

    private List<MatchEntity> getMatchsPublicsPotentiellementFacturables() {
        List<MatchEntity> matchs = new ArrayList<>();

        for (MatchStatus statut : STATUTS_MATCH_PUBLIC_FACTURABLE) {
            matchs.addAll(
                    matchRepository.findByTypeMatchAndStatut(
                            MatchType.PUBLIC,
                            statut
                    )
            );
        }

        return matchs;
    }

    private LocalDateTime getDateHeureMatch(MatchEntity match) {
        if (match == null
                || match.getDateMatch() == null
                || match.getHeureDebut() == null) {
            return null;
        }

        return LocalDateTime.of(
                match.getDateMatch(),
                match.getHeureDebut()
        );
    }

    private long compterJoueursActifs(MatchEntity match) {
        if (match == null || match.getParticipations() == null) {
            return 0;
        }

        return match.getParticipations()
                .stream()
                .filter(this::estParticipationActive)
                .count();
    }

    private boolean estParticipationActive(
            ParticipationEntity participation
    ) {
        return participation != null
                && participation.getStatut() != null
                && STATUTS_PARTICIPATION_ACTIVE.contains(
                participation.getStatut()
        );
    }

    private boolean peutEtreRenduPublic(MatchEntity match) {
        return match != null
                && match.getStatut() != null
                && STATUTS_MATCH_RENDABLE_PUBLIC.contains(
                match.getStatut()
        );
    }

    private void convertirEnMatchPublic(MatchEntity match) {
        if (match == null) {
            return;
        }

        match.setTypeMatch(MatchType.PUBLIC);
        match.setStatut(MatchStatus.OUVERT);

        matchRepository.save(match);
    }

    private void creerPenaliteOrganisateur(
            MatchEntity match,
            String raison
    ) {
        if (match == null || match.getOrganisateur() == null) {
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
        LocalDate dateFin = dateDebut.plusDays(
                ClubBusinessRules.PENALTY_DURATION_DAYS
        );

        PenaliteEntity penalite = new PenaliteEntity();

        penalite.setMembre(match.getOrganisateur());
        penalite.setMatch(match);
        penalite.setDateDebut(dateDebut);
        penalite.setDateFin(dateFin);
        penalite.setActive(true);
        penalite.setRaison(raison);

        penaliteRepository.save(penalite);

        LOGGER.info(
                "Pénalité créée : membreId={}, matchId={}, dateFin={}",
                match.getOrganisateur().getId(),
                match.getId(),
                dateFin
        );
    }

    private void creerDettePourParticipationImpayee(
            ParticipationEntity participation,
            MatchEntity match
    ) {
        if (participation == null || participation.getMembre() == null) {
            LOGGER.warn(
                    "Dette non créée : participation invalide ou membre absent."
            );
            return;
        }

        if (participation.getMontantDuCentimes() == null
                || participation.getMontantDuCentimes() <= 0) {
            LOGGER.warn(
                    "Dette non créée : montant invalide pour participationId={}.",
                    participation.getId()
            );
            return;
        }

        paiementService.createDebt(
                participation.getMembre(),
                participation,
                match != null ? match.getReservation() : null,
                participation.getMontantDuCentimes(),
                DetteRaison.PARTICIPATION_IMPAYEE
        );
    }

    private boolean estOrganisateur(
            MatchEntity match,
            ParticipationEntity participation
    ) {
        return match != null
                && participation != null
                && match.getOrganisateur() != null
                && participation.getMembre() != null
                && match.getOrganisateur().getId().equals(
                participation.getMembre().getId()
        );
    }
}