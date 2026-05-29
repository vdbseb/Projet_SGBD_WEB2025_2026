package be.angularpadelclub.Service;

import be.angularpadelclub.Entity.MatchEntity;
import be.angularpadelclub.Entity.ParticipationEntity;
import be.angularpadelclub.Enum.DetteRaison;
import be.angularpadelclub.Enum.MatchStatus;
import be.angularpadelclub.Enum.MatchType;
import be.angularpadelclub.Enum.ParticipationStatut;
import be.angularpadelclub.Repository.MatchRepository;
import be.angularpadelclub.Repository.ParticipationRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;

@Service
public class MatchSchedulerService {


    private final MatchRepository matchRepository;
    private final ParticipationRepository participationRepository;
    private final PaiementService paiementService;

    public MatchSchedulerService(
            MatchRepository matchRepository,
            ParticipationRepository participationRepository,
            PaiementService paiementService
    ) {
        this.matchRepository = matchRepository;
        this.participationRepository = participationRepository;
        this.paiementService = paiementService;
    }

    @Scheduled(fixedRate = 300000) // toutes les 5 min
    //@Scheduled(fixedRate = 10000) // toutes les 10 sec
    @Transactional
    public void convertirMatchsPrivesIncompletsEnPublics() {

        LocalDateTime maintenant = LocalDateTime.now();
        LocalDateTime limite = maintenant.plusHours(24);

        List<MatchEntity> matchs =
                matchRepository.findByTypeMatchAndStatut(
                        MatchType.PRIVE,
                        MatchStatus.PLANIFIE
                );

        for (MatchEntity match : matchs) {

            LocalDateTime dateHeureMatch =
                    LocalDateTime.of(
                            match.getDateMatch(),
                            match.getHeureDebut()
                    );

            int nbJoueurs =
                    match.getParticipations().size();

            boolean doitPasserPublic =
                    dateHeureMatch.isBefore(limite)
                            && nbJoueurs < 4;

            if (doitPasserPublic) {

                System.out.println(
                        "Match "
                                + match.getId()
                                + " passe PUBLIC"
                );

                match.setTypeMatch(
                        MatchType.PUBLIC
                );

                match.setStatut(
                        MatchStatus.OUVERT
                );
            }
        }
    }

    @Scheduled(fixedRate = 300000)
    @Transactional
    public void libererPlacesNonPayees() {
        List<ParticipationEntity> overdueParticipations =
                participationRepository.findByStatutAndDateLimitePaiementBefore(
                        ParticipationStatut.EN_ATTENTE_PAIEMENT,
                        LocalDateTime.now()
                );

        for (ParticipationEntity participation : overdueParticipations) {
            MatchEntity match = participation.getMatch();

            paiementService.createDebt(
                    participation.getMembre(),
                    participation,
                    match.getReservation(),
                    participation.getMontantDuCentimes(),
                    DetteRaison.PARTICIPATION_IMPAYEE
            );

            boolean isOrganizer = match.getOrganisateur() != null
                    && match.getOrganisateur()
                    .getId()
                    .equals(participation.getMembre().getId());

            participation.setStatut(ParticipationStatut.LIBEREE);

            if (!isOrganizer) {
                match.setTypeMatch(MatchType.PUBLIC);
                match.setStatut(MatchStatus.OUVERT);
            }
        }
    }
}
