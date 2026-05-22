package be.angularpadelclub.DTO;

import java.util.UUID;

public record CourtDTO(
        Integer id,
        String name,
        Integer siteId,
        boolean indoor,
        boolean active
) {
}