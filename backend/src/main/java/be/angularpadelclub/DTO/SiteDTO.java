package be.angularpadelclub.DTO;

import java.time.LocalTime;
import java.util.List;


public record SiteDTO(
        Integer id,
        String name,
        String city,
        String adresse,
        String codePostal,
        String description,
        LocalTime openingTime,
        LocalTime closingTime,
        Boolean active,
        String imageUrl,
        List<CourtDTO> courts
) {
}