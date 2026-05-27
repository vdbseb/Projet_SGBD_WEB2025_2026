package be.angularpadelclub.Service;

import be.angularpadelclub.DTO.MatchDTO;
import be.angularpadelclub.Entity.CourtEntity;
import be.angularpadelclub.Entity.MatchEntity;
import be.angularpadelclub.Entity.MembreEntity;
import be.angularpadelclub.Entity.ParticipationEntity;
import be.angularpadelclub.Entity.ReservationEntity;
import be.angularpadelclub.Enum.MatchStatus;
import be.angularpadelclub.Enum.MatchType;
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

    private final MatchRepository matchRepository;
    private final CourtRepository courtRepository;
    private final MembreRepository membreRepository;
    private final ParticipationRepository participationRepository;
    private final ReservationRepository reservationRepository;

    public MatchService(
            MatchRepository matchRepository,
            CourtRepository courtRepository,
            MembreRepository membreRepository,
            ParticipationRepository participationRepository,
            ReservationRepository reservationRepository
    ) {
        this.matchRepository = matchRepository;
        this.courtRepository = courtRepository;
        this.membreRepository = membreRepository;
        this.participationRepository = participationRepository;
        this.reservationRepository = reservationRepository;
    }

    public MatchEntity createMatch(MatchDTO dto) {

        CourtEntity court = courtRepository.findById(dto.terrainId())
                .orElseThrow(() -> new RuntimeException(
                        "Terrain introuvable"
                ));

        MembreEntity organisateur =
                membreRepository.findById(dto.organisateurId())
                        .orElseThrow(() -> new RuntimeException(
                                "Organisateur introuvable"
                        ));

        int nombreJoueurs = 1;

        if (dto.playerMatricules() != null) {
            nombreJoueurs += dto.playerMatricules().size();
        }

        if (dto.matchType() == MatchType.PRIVE
                && nombreJoueurs != 4) {

            throw new RuntimeException(
                    "Un match privé doit avoir exactement 4 joueurs"
            );
        }

        if (nombreJoueurs > 4) {
            throw new RuntimeException(
                    "Un match ne peut pas avoir plus de 4 joueurs"
            );
        }

        // Création réservation
        ReservationEntity reservation =
                new ReservationEntity();

        reservation.setCourt(court);
        reservation.setMember(organisateur);
        reservation.setDate(dto.dateMatch());
        reservation.setStartTime(dto.heureDebut());
        reservation.setEndTime(
                dto.heureDebut().plusMinutes(90)
        );

        ReservationEntity savedReservation =
                reservationRepository.save(reservation);

        // Création match
        MatchEntity match = new MatchEntity();

        match.setTerrain(court);
        match.setOrganisateur(organisateur);
        match.setDateMatch(dto.dateMatch());
        match.setHeureDebut(dto.heureDebut());
        match.setHeureFin(
                dto.heureDebut().plusMinutes(90)
        );
        match.setTypeMatch(dto.matchType());
        match.setStatut(MatchStatus.OUVERT);
        match.setPrixTotal(60);
        match.setCreatedAt(LocalDateTime.now());

        // Lien réservation ↔ match
        match.setReservation(savedReservation);

        MatchEntity savedMatch =
                matchRepository.save(match);

        createParticipation(
                savedMatch,
                organisateur
        );

        if (dto.playerMatricules() != null) {

            for (String matricule :
                    dto.playerMatricules()) {

                MembreEntity player =
                        membreRepository
                                .findByMatricule(matricule)
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "Joueur introuvable : "
                                                        + matricule
                                        ));

                createParticipation(
                        savedMatch,
                        player
                );
            }
        }

        return savedMatch;
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
        participation.setPaiement(null);

        participationRepository.save(
                participation
        );
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
                                        "Match introuvable avec l'id "
                                                + matchId
                                ));

        if (match.getStatut()
                == MatchStatus.ANNULE) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ce match est déjà annulé"
            );
        }

        if (match.getStatut()
                == MatchStatus.TERMINE) {

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

        return matchRepository.save(match);
    }

    @Transactional
    public void joinPublicMatch(Integer matchId, Integer memberId) {

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

        if (match.getStatut() == MatchStatus.COMPLET) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Impossible de rejoindre un match complet."
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
                participationRepository.existsByMatch_IdAndMembre_Id(
                        matchId,
                        memberId
                );

        if (alreadyRegistered) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ce membre est déjà inscrit à ce match."
            );
        }

        int nombreParticipants =
                participationRepository.countByMatch_Id(matchId);

        if (nombreParticipants >= 4) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Impossible de rejoindre ce match : il est déjà complet."
            );
        }

        ParticipationEntity participation = new ParticipationEntity();
        participation.setMatch(match);
        participation.setMembre(membre);
        participation.setDateInscription(LocalDateTime.now());
        participation.setPaiement(null);

        participationRepository.save(participation);

        if (nombreParticipants + 1 == 4) {
            match.setStatut(MatchStatus.COMPLET);
            matchRepository.save(match);
        }
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

//        if (match.getTypeMatch() != MatchType.PUBLIC) {
//            throw new ResponseStatusException(
//                    HttpStatus.CONFLICT,
//                    "Impossible de quitter un match privé."
//            );
//        }

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
                        .findByMatch_IdAndMembre_Id(
                                matchId,
                                memberId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Participation introuvable."
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

        participationRepository.delete(participation);

        int nombreParticipants =
                participationRepository.countByMatch_Id(matchId);

        if (nombreParticipants < 4
                && match.getStatut() == MatchStatus.COMPLET) {

            if (match.getTypeMatch() == MatchType.PUBLIC) {
                match.setStatut(MatchStatus.OUVERT);
            } else {
                match.setStatut(MatchStatus.PLANIFIE);
            }

            matchRepository.save(match);
        }
    }
}