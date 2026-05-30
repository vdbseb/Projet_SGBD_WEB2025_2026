package be.angularpadelclub.Service;

import be.angularpadelclub.Entity.MatchEntity;
import be.angularpadelclub.Entity.MembreEntity;
import be.angularpadelclub.Entity.ParticipationEntity;
import be.angularpadelclub.Enum.ParticipationStatut;
import be.angularpadelclub.Repository.ParticipationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class ParticipationService {

    private final ParticipationRepository participationRepository;

    public ParticipationService(
            ParticipationRepository participationRepository
    ) {
        this.participationRepository = participationRepository;
    }

    public ParticipationEntity createPendingParticipation(
            MatchEntity match,
            MembreEntity membre
    ) {
        validatePendingParticipationInput(match, membre);

        ParticipationEntity participation = new ParticipationEntity();

        participation.setMatch(match);
        participation.setMembre(membre);
        participation.setDateInscription(LocalDateTime.now());
        participation.setStatut(ParticipationStatut.EN_ATTENTE_PAIEMENT);
        participation.setMontantDuCentimes(
                ClubBusinessRules.DEFAULT_PLAYER_SHARE_CENTS
        );
        participation.setDateLimitePaiement(
                LocalDateTime.of(
                        match.getDateMatch().minusDays(1),
                        match.getHeureDebut()
                )
        );

        return participationRepository.save(participation);
    }

    public boolean isActiveParticipation(ParticipationStatut statut) {
        return statut == ParticipationStatut.EN_ATTENTE_PAIEMENT
                || statut == ParticipationStatut.PAYEE;
    }

    private void validatePendingParticipationInput(
            MatchEntity match,
            MembreEntity membre
    ) {
        if (match == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Création de participation impossible : le match est obligatoire."
            );
        }

        if (membre == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Création de participation impossible : le membre est obligatoire."
            );
        }

        if (match.getDateMatch() == null || match.getHeureDebut() == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Création de participation impossible : le match n'a pas de date ou d'heure de début."
            );
        }
    }
}