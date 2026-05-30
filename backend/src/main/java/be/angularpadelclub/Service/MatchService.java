package be.angularpadelclub.Service;

import be.angularpadelclub.Entity.MatchEntity;
import be.angularpadelclub.Entity.MembreEntity;
import be.angularpadelclub.Entity.ParticipationEntity;
import be.angularpadelclub.Enum.MatchStatus;
import be.angularpadelclub.Enum.MatchType;
import be.angularpadelclub.Enum.ParticipationStatut;
import be.angularpadelclub.Repository.MatchRepository;
import be.angularpadelclub.Repository.ParticipationRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MatchService {

    private static final List<ParticipationStatut> ACTIVE_PARTICIPATION_STATUSES =
            List.of(
                    ParticipationStatut.EN_ATTENTE_PAIEMENT,
                    ParticipationStatut.PAYEE
            );

    private final MatchRepository matchRepository;
    private final ParticipationRepository participationRepository;
    private final PaiementService paiementService;
    private final ReferenceLookupService referenceLookupService;
    private final ParticipationService participationService;

    public MatchService(
            MatchRepository matchRepository,
            ParticipationRepository participationRepository,
            PaiementService paiementService,
            ReferenceLookupService referenceLookupService,
            ParticipationService participationService
    ) {
        this.matchRepository = matchRepository;
        this.participationRepository = participationRepository;
        this.paiementService = paiementService;
        this.referenceLookupService = referenceLookupService;
        this.participationService = participationService;
    }

    public List<MatchEntity> findAll() {
        return matchRepository.findAll();
    }

    @Transactional
    public MatchEntity annulerMatch(
            Integer matchId,
            String matricule
    ) {
        MatchEntity match = referenceLookupService.findMatchOrThrow(matchId);

        if (match.getStatut() == MatchStatus.ANNULE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ce match est déjà annulé."
            );
        }

        if (match.getStatut() == MatchStatus.TERMINE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Impossible d'annuler un match terminé."
            );
        }

        if (matricule == null || matricule.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le matricule de l'organisateur est obligatoire."
            );
        }

        boolean estOrganisateur =
                match.getOrganisateur() != null
                        && matricule.equals(match.getOrganisateur().getMatricule());

        if (!estOrganisateur) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Seul l'organisateur peut annuler ce match."
            );
        }

        match.setStatut(MatchStatus.ANNULE);

        paiementService.rembourserPaiementsMatch(match.getId());

        return matchRepository.save(match);
    }

    @Transactional
    public ParticipationEntity joinPublicMatch(
            Integer matchId,
            Integer memberId
    ) {
        MatchEntity match = referenceLookupService.findMatchOrThrow(matchId);
        MembreEntity membre = referenceLookupService.findMembreOrThrow(memberId);

        validateMatchCanBeJoined(match);
        validateMemberCanJoin(membre);
        validateMemberIsNotAlreadyRegistered(matchId, memberId);

        int nombreParticipantsActifs =
                participationRepository.countByMatch_IdAndStatutIn(
                        matchId,
                        ACTIVE_PARTICIPATION_STATUSES
                );

        if (nombreParticipantsActifs >= ClubBusinessRules.MAX_PLAYERS_PER_MATCH) {
            match.setStatut(MatchStatus.COMPLET);
            matchRepository.save(match);

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Impossible de rejoindre ce match : il est déjà complet."
            );
        }

        ParticipationEntity savedParticipation =
                participationService.createPendingParticipation(match, membre);

        updateMatchStatusAfterParticipantCount(
                match,
                nombreParticipantsActifs + 1
        );

        matchRepository.save(match);

        return savedParticipation;
    }

    @Transactional
    public void leaveMatch(
            Integer matchId,
            Integer memberId
    ) {
        MatchEntity match = referenceLookupService.findMatchOrThrow(matchId);
        referenceLookupService.findMembreOrThrow(memberId);

        validateMatchCanBeLeft(match);

        ParticipationEntity participation =
                participationRepository
                        .findFirstByMatch_IdAndMembre_IdAndStatutIn(
                                matchId,
                                memberId,
                                ACTIVE_PARTICIPATION_STATUSES
                        )
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Participation active introuvable pour ce membre."
                        ));

        if (isOrganizer(match, memberId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "L'organisateur ne peut pas quitter son propre match. Il doit l'annuler."
            );
        }

        paiementService.rembourserPaiementsParticipation(participation.getId());

        participation.setStatut(ParticipationStatut.LIBEREE);
        participationRepository.save(participation);

        int nombreParticipantsActifs =
                participationRepository.countByMatch_IdAndStatutIn(
                        matchId,
                        ACTIVE_PARTICIPATION_STATUSES
                );

        updateMatchStatusAfterParticipantCount(
                match,
                nombreParticipantsActifs
        );

        matchRepository.save(match);
    }

    private void validateMatchCanBeJoined(MatchEntity match) {
        if (match.getTypeMatch() != MatchType.PUBLIC) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Impossible de rejoindre un match privé."
            );
        }

        if (match.getStatut() == MatchStatus.ANNULE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Impossible de rejoindre un match annulé."
            );
        }

        if (match.getStatut() == MatchStatus.TERMINE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Impossible de rejoindre un match terminé."
            );
        }
    }

    private void validateMatchCanBeLeft(MatchEntity match) {
        if (match.getStatut() == MatchStatus.ANNULE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Impossible de quitter un match annulé."
            );
        }

        if (match.getStatut() == MatchStatus.TERMINE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Impossible de quitter un match terminé."
            );
        }
    }

    private void validateMemberCanJoin(MembreEntity membre) {
        if (!membre.isActif()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Inscription impossible : membre inactif."
            );
        }
    }

    private void validateMemberIsNotAlreadyRegistered(
            Integer matchId,
            Integer memberId
    ) {
        boolean alreadyRegistered =
                participationRepository.existsByMatch_IdAndMembre_IdAndStatutIn(
                        matchId,
                        memberId,
                        ACTIVE_PARTICIPATION_STATUSES
                );

        if (alreadyRegistered) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ce membre est déjà inscrit à ce match."
            );
        }
    }

    private void updateMatchStatusAfterParticipantCount(
            MatchEntity match,
            int nombreParticipantsActifs
    ) {
        if (nombreParticipantsActifs >= ClubBusinessRules.MAX_PLAYERS_PER_MATCH) {
            match.setStatut(MatchStatus.COMPLET);
            return;
        }

        if (match.getTypeMatch() == MatchType.PUBLIC) {
            match.setStatut(MatchStatus.OUVERT);
            return;
        }

        match.setStatut(MatchStatus.PLANIFIE);
    }

    private boolean isOrganizer(
            MatchEntity match,
            Integer memberId
    ) {
        return match.getOrganisateur() != null
                && match.getOrganisateur().getId().equals(memberId);
    }
}