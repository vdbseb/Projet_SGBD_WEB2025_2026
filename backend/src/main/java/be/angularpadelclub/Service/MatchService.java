package be.angularpadelclub.Service;

import be.angularpadelclub.DTO.MatchDTO;
import be.angularpadelclub.Entity.CourtEntity;
import be.angularpadelclub.Entity.MatchEntity;
import be.angularpadelclub.Entity.MembreEntity;
import be.angularpadelclub.Entity.ParticipationEntity;
import be.angularpadelclub.Entity.ReservationEntity;
import be.angularpadelclub.Enum.MatchStatus;
import be.angularpadelclub.Enum.MatchType;
import be.angularpadelclub.Enum.ParticipationStatut;
import be.angularpadelclub.Repository.CourtRepository;
import be.angularpadelclub.Repository.MatchRepository;
import be.angularpadelclub.Repository.MembreRepository;
import be.angularpadelclub.Repository.ParticipationRepository;
import be.angularpadelclub.Repository.ReservationRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MatchService {

    private static final int PLAYER_SHARE_CENTS = 1500;

    private static final List<ParticipationStatut> ACTIVE_PARTICIPATION_STATUSES =
            List.of(
                    ParticipationStatut.EN_ATTENTE_PAIEMENT,
                    ParticipationStatut.PAYEE
            );

    private final MatchRepository matchRepository;
    private final CourtRepository courtRepository;
    private final MembreRepository membreRepository;
    private final ParticipationRepository participationRepository;
    private final ReservationRepository reservationRepository;
    private final PaiementService paiementService;

    public MatchService(
            MatchRepository matchRepository,
            CourtRepository courtRepository,
            MembreRepository membreRepository,
            ParticipationRepository participationRepository,
            ReservationRepository reservationRepository,
            PaiementService paiementService
    ) {
        this.matchRepository = matchRepository;
        this.courtRepository = courtRepository;
        this.membreRepository = membreRepository;
        this.participationRepository = participationRepository;
        this.reservationRepository = reservationRepository;
        this.paiementService = paiementService;
    }

    public List<MatchEntity> findAll() {
        return matchRepository.findAll();
    }

    private void createParticipation(
            MatchEntity match,
            MembreEntity membre
    ) {
        ParticipationEntity participation =
                new ParticipationEntity();

        participation.setMatch(match);
        participation.setMembre(membre);
        participation.setDateInscription(
                LocalDateTime.now()
        );
        participation.setStatut(ParticipationStatut.EN_ATTENTE_PAIEMENT);
        participation.setMontantDuCentimes(PLAYER_SHARE_CENTS);
        participation.setDateLimitePaiement(
                LocalDateTime.of(
                        match.getDateMatch().minusDays(1),
                        match.getHeureDebut()
                )
        );

        participationRepository.save(participation);
    }

    @Transactional
    public MatchEntity annulerMatch(
            Integer matchId,
            String matricule
    ) {
        MatchEntity match =
                matchRepository.findById(matchId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Match introuvable avec l'id " + matchId
                                ));

        if (match.getStatut() == MatchStatus.ANNULE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ce match est déjà annulé"
            );
        }

        if (match.getStatut() == MatchStatus.TERMINE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Impossible d'annuler un match terminé"
            );
        }

        boolean estOrganisateur =
                match.getOrganisateur() != null
                        && match.getOrganisateur()
                        .getMatricule()
                        .equals(matricule);

        if (!estOrganisateur) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Seul l'organisateur peut annuler ce match"
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
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Match introuvable avec l'id " + matchId
                ));

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

        MembreEntity membre = membreRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Membre introuvable avec l'id " + memberId
                ));

        if (!membre.isActif()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Inscription impossible : membre inactif."
            );
        }

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

        int nombreParticipantsActifs =
                participationRepository.countByMatch_IdAndStatutIn(
                        matchId,
                        ACTIVE_PARTICIPATION_STATUSES
                );

        if (nombreParticipantsActifs >= 4) {
            match.setStatut(MatchStatus.COMPLET);
            matchRepository.save(match);

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Impossible de rejoindre ce match : il est déjà complet."
            );
        }

        ParticipationEntity participation = new ParticipationEntity();
        participation.setMatch(match);
        participation.setMembre(membre);
        participation.setDateInscription(LocalDateTime.now());
        participation.setStatut(ParticipationStatut.EN_ATTENTE_PAIEMENT);
        participation.setMontantDuCentimes(PLAYER_SHARE_CENTS);
        participation.setDateLimitePaiement(
                LocalDateTime.of(
                        match.getDateMatch().minusDays(1),
                        match.getHeureDebut()
                )
        );

        ParticipationEntity savedParticipation =
                participationRepository.save(participation);

        int nombreParticipantsApresInscription =
                nombreParticipantsActifs + 1;

        if (nombreParticipantsApresInscription >= 4) {
            match.setStatut(MatchStatus.COMPLET);
        } else {
            match.setStatut(MatchStatus.OUVERT);
        }

        matchRepository.save(match);

        return savedParticipation;
    }

    @Transactional
    public void leaveMatch(
            Integer matchId,
            Integer memberId
    ) {
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Match introuvable avec l'id " + matchId
                ));

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

        ParticipationEntity participation =
                participationRepository
                        .findFirstByMatch_IdAndMembre_IdAndStatutIn(
                                matchId,
                                memberId,
                                ACTIVE_PARTICIPATION_STATUSES
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Participation active introuvable pour ce membre."
                                ));

        boolean estOrganisateur =
                match.getOrganisateur() != null
                        && match.getOrganisateur()
                        .getId()
                        .equals(memberId);

        if (estOrganisateur) {
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

        if (nombreParticipantsActifs >= 4) {
            match.setStatut(MatchStatus.COMPLET);
        } else if (match.getTypeMatch() == MatchType.PUBLIC) {
            match.setStatut(MatchStatus.OUVERT);
        } else {
            match.setStatut(MatchStatus.PLANIFIE);
        }

        matchRepository.save(match);
    }
}