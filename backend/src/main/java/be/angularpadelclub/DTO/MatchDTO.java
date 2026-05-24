package be.angularpadelclub.DTO;

import be.angularpadelclub.Enum.MatchStatus;
import be.angularpadelclub.Enum.MatchType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record MatchDTO(
        Integer id,
        Integer terrainId,
        Integer organisateurId,
        LocalDate dateMatch,
        LocalTime heureDebut,
        LocalTime heureFin,
        MatchType matchType,
        MatchStatus statut,
        Integer prixTotal,
        List<String> playerMatricules
) {}
