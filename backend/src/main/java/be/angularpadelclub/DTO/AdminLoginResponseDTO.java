package be.angularpadelclub.DTO;

public record AdminLoginResponseDTO(
        String token,
        AdministrateurDTO admin
) {
}