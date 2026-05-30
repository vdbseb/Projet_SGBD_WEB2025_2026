package be.angularpadelclub.DTO;

public record CourtDTO(
        Integer id,
        String name,
        Integer siteId,
        boolean indoor,
        boolean active,
        boolean maintenance
) {
}