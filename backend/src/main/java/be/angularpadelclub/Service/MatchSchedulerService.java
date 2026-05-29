package be.angularpadelclub.Service;

import be.angularpadelclub.Entity.MatchEntity;
import be.angularpadelclub.Enum.MatchStatus;
import be.angularpadelclub.Enum.MatchType;
import be.angularpadelclub.Repository.MatchRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;

@Service
public class MatchSchedulerService {


    private final MatchRepository matchRepository;

    public MatchSchedulerService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
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
}