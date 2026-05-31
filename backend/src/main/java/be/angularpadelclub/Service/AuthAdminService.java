package be.angularpadelclub.Service;

import be.angularpadelclub.DTO.AdminLoginRequestDTO;
import be.angularpadelclub.DTO.AdminLoginResponseDTO;
import be.angularpadelclub.Entity.AdministrateurEntity;
import be.angularpadelclub.Mapper.AdministrateurMapper;
import be.angularpadelclub.Repository.AdministrateurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthAdminService {

    private final AdministrateurRepository administrateurRepository;
    private final AdministrateurMapper administrateurMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthAdminService(
            AdministrateurRepository administrateurRepository,
            AdministrateurMapper administrateurMapper,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.administrateurRepository = administrateurRepository;
        this.administrateurMapper = administrateurMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AdminLoginResponseDTO loginAdmin(AdminLoginRequestDTO request) {
        String matricule = normalizeMatricule(request.matricule());

        AdministrateurEntity admin = administrateurRepository
                .findByMatricule(matricule)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Matricule ou mot de passe administrateur incorrect."
                ));

        if (!passwordEncoder.matches(request.password(), admin.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Matricule ou mot de passe administrateur incorrect."
            );
        }

        String token = jwtService.generateAdminToken(admin);

        return new AdminLoginResponseDTO(
                token,
                administrateurMapper.toDTO(admin)
        );
    }

    private String normalizeMatricule(String matricule) {
        if (matricule == null) {
            return "";
        }

        return matricule.trim().toUpperCase();
    }
}