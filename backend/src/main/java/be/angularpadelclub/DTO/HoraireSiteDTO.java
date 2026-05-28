package be.angularpadelclub.DTO;

import java.time.LocalTime;

public record HoraireSiteDTO(
        Integer id,
        Integer siteId,
        String siteNom,
        int annee,
        LocalTime heure_debut,
        LocalTime heure_fin,
        int duree_match_minutes,
        int pause_minutes
) {}