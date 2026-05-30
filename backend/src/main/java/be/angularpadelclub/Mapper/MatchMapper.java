package be.angularpadelclub.Mapper;

import be.angularpadelclub.DTO.MatchDTO;
import be.angularpadelclub.DTO.ReservationDTO;
import be.angularpadelclub.Entity.CourtEntity;
import be.angularpadelclub.Entity.MatchEntity;
import be.angularpadelclub.Entity.MembreEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MatchMapper {

    public MatchDTO toDTO(MatchEntity match) {
        if (match == null) {
            return null;
        }

        List<String> playerMatricules = match.getParticipations() == null
                ? List.of()
                : match.getParticipations()
                .stream()
                .filter(participation -> participation.getMembre() != null)
                .map(participation -> participation.getMembre().getMatricule())
                .toList();

        return new MatchDTO(
                match.getId(),
                match.getTerrain() != null ? match.getTerrain().getId() : null,
                match.getTerrain() != null ? match.getTerrain().getNom() : null,
                match.getOrganisateur() != null ? match.getOrganisateur().getId() : null,
                match.getDateMatch(),
                match.getHeureDebut(),
                match.getHeureFin(),
                match.getTypeMatch(),
                match.getStatut(),
                match.getPrixTotal(),
                playerMatricules
        );
    }

    public static MatchEntity toEntity(
            ReservationDTO reservation,
            CourtEntity court,
            MembreEntity organizer
    ) {
        if (reservation == null) {
            return null;
        }

        MatchEntity match = new MatchEntity();

        match.setTerrain(court);
        match.setOrganisateur(organizer);
        match.setDateMatch(reservation.date());
        match.setHeureDebut(reservation.startTime());
        match.setHeureFin(reservation.endTime());
        match.setTypeMatch(reservation.matchType());

        return match;
    }
}