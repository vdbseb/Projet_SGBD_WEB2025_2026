package be.angularpadelclub.DTO;

import java.time.LocalDate;

public record JourFermetureDTO(
        Integer id,
        Integer siteId,
        String siteNom,
        LocalDate dateFermeture,
        String raison,
        boolean global
) {}