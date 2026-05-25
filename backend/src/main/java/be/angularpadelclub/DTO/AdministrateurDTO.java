package be.angularpadelclub.DTO;

public record AdministrateurDTO(
        Integer id,
        String matricule,
        String nom,
        String prenom,
        String email,
        String typeAdmin,
        Integer siteId,
        String siteNom
) {
}