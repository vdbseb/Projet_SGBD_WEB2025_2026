package be.angularpadelclub.DTO;


import be.angularpadelclub.Entity.TypeMembreEntity;

public record MemberDTO(
        Integer id,
        boolean active,
        String email,
        String matricule,
        String firstName,
        String lastName,
        TypeMembreEntity type,
        Integer siteId,
        String siteName
) {
}