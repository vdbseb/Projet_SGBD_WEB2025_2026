package be.angularpadelclub.Service;

import be.angularpadelclub.DTO.MatchDTO;
import be.angularpadelclub.Entity.CourtEntity;
import be.angularpadelclub.Entity.MatchEntity;
import be.angularpadelclub.Entity.MemberEntity;
import be.angularpadelclub.Entity.ParticipationEntity;
import be.angularpadelclub.Enum.MatchStatus;
import be.angularpadelclub.Enum.MatchType;
import be.angularpadelclub.Repository.CourtRepository;
import be.angularpadelclub.Repository.MatchRepository;
import be.angularpadelclub.Repository.MemberRepository;
import be.angularpadelclub.Repository.ParticipationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final CourtRepository courtRepository;
    private final MemberRepository memberRepository;
    private final ParticipationRepository participationRepository;

    public MatchService(
            MatchRepository matchRepository,
            CourtRepository courtRepository,
            MemberRepository memberRepository,
            ParticipationRepository participationRepository
    ) {
        this.matchRepository = matchRepository;
        this.courtRepository = courtRepository;
        this.memberRepository = memberRepository;
        this.participationRepository = participationRepository;
    }

    public MatchEntity createMatch(MatchDTO dto) {

        CourtEntity court = courtRepository.findById(dto.terrainId())
                .orElseThrow(() -> new RuntimeException("Terrain introuvable"));

        MemberEntity organisateur = memberRepository.findById(dto.organisateurId())
                .orElseThrow(() -> new RuntimeException("Organisateur introuvable"));

        int nombreJoueurs = 1;

        if (dto.playerMatricules() != null) {
            nombreJoueurs += dto.playerMatricules().size();
        }

        if (dto.matchType() == MatchType.PRIVE && nombreJoueurs != 4) {
            throw new RuntimeException("Un match privé doit avoir exactement 4 joueurs");
        }

        if (nombreJoueurs > 4) {
            throw new RuntimeException("Un match ne peut pas avoir plus de 4 joueurs");
        }

        MatchEntity match = new MatchEntity();

        match.setTerrain(court);
        match.setOrganisateur(organisateur);
        match.setDateMatch(dto.dateMatch());
        match.setHeureDebut(dto.heureDebut());
        match.setHeureFin(dto.heureDebut().plusMinutes(90));
        match.setTypeMatch(dto.matchType());
        match.setStatut(MatchStatus.OUVERT);
        match.setPrixTotal(60);
        match.setCreatedAt(LocalDateTime.now());

        MatchEntity savedMatch = matchRepository.save(match);

        createParticipation(savedMatch, organisateur);

        if (dto.playerMatricules() != null) {
            for (String matricule : dto.playerMatricules()) {
                MemberEntity player = memberRepository.findByMatricule(matricule)
                        .orElseThrow(() -> new RuntimeException("Joueur introuvable : " + matricule));

                createParticipation(savedMatch, player);
            }
        }

        return savedMatch;
    }

    public List<MatchEntity> findAll() {
        return matchRepository.findAll();
    }

    private void createParticipation(MatchEntity match, MemberEntity membre) {
        ParticipationEntity participation = new ParticipationEntity();
        participation.setMatch(match);
        participation.setMembre(membre);
        participation.setDateInscription(LocalDateTime.now());
        participation.setPaiement(null);

        participationRepository.save(participation);
    }
}