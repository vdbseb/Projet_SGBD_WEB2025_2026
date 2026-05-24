package be.angularpadelclub.Mapper;

import be.angularpadelclub.DTO.MatchDTO;
import be.angularpadelclub.Entity.MatchEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MatchMapper {

    public MatchDTO toDTO(MatchEntity match) {
        List<String> playerMatricules = match.getParticipations() == null
                ? List.of()
                : match.getParticipations()
                .stream()
                .map(p -> p.getMembre().getMatricule())
                .toList();

        return new MatchDTO(
                match.getId(),
                match.getTerrain().getId(),
                match.getOrganisateur().getId(),
                match.getDateMatch(),
                match.getHeureDebut(),
                match.getHeureFin(),
                match.getTypeMatch(),
                match.getStatut(),
                match.getPrixTotal(),
                playerMatricules
        );
    }
}