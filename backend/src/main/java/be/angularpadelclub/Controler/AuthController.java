package be.angularpadelclub.Controler;

import be.angularpadelclub.DTO.AdminLoginRequestDTO;
import be.angularpadelclub.DTO.AdminLoginResponseDTO;
import be.angularpadelclub.Service.AuthAdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final AuthAdminService authAdminService;

    public AuthController(AuthAdminService authAdminService) {
        this.authAdminService = authAdminService;
    }

    @PostMapping(value = "/admin/login", produces = "application/json")
    public AdminLoginResponseDTO loginAdmin(
            @Valid @RequestBody AdminLoginRequestDTO request
    ) {
        return authAdminService.loginAdmin(request);
    }
}