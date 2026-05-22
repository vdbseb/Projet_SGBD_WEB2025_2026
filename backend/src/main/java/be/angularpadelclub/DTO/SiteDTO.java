package be.angularpadelclub.DTO;

import java.time.LocalTime;


public record SiteDTO(
        Integer id,
        String name,
        String city,
        String adresse,
        String description,
        LocalTime openingTime,
        LocalTime closingTime,
        boolean active,
        String imageURL
) {
}