package be.angularpadelclub.Controler;

import be.angularpadelclub.DTO.MembreDTO;
import be.angularpadelclub.Mapper.MembreMapper;
import be.angularpadelclub.Service.MembreService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@CrossOrigin(origins = "http://localhost:4200")
public class MembreController {

    private final MembreService membreService;
    private final MembreMapper membreMapper;

    public MembreController(
            MembreService membreService,
            MembreMapper membreMapper
    ) {
        this.membreService = membreService;
        this.membreMapper = membreMapper;
    }

    @GetMapping(produces = "application/json")
    public List<MembreDTO> findAll() {
        return membreMapper.toDTOList(
                membreService.findAll()
        );
    }

    @GetMapping(value = "/search", produces = "application/json")
    public List<MembreDTO> findByNomAndPrenom(
            @RequestParam("nom") String nom,
            @RequestParam("prenom") String prenom
    ) {
        return membreService.findByNomAndPrenom(nom, prenom)
                .stream()
                .map(membreMapper::toDTO)
                .toList();
    }

    @GetMapping(
            value = "/next-matricule",
            produces = "text/plain"
    )
    public String getNextMatricule(
            @RequestParam("typeCode") String typeCode
    ) {
        return membreService.getNextMatricule(typeCode);
    }

    @GetMapping(value = "/{matricule}", produces = "application/json")
    public MembreDTO findByMatricule(
            @PathVariable("matricule") String matricule
    ) {
        return membreService.findByMatricule(matricule)
                .map(membreMapper::toDTO)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Membre introuvable avec le matricule : " + matricule
                ));
    }

    @GetMapping(value = "/admin/{adminMatricule}", produces = "application/json")
    public List<MembreDTO> findVisibleByAdmin(
            @PathVariable("adminMatricule") String adminMatricule
    ) {
        return membreMapper.toDTOList(
                membreService.findVisibleByAdmin(adminMatricule)
        );
    }

    @PostMapping(
            value = "/admin/{adminMatricule}",
            consumes = "application/json"
    )
    @ResponseStatus(HttpStatus.CREATED)
    public void addMemberAsAdmin(
            @PathVariable("adminMatricule") String adminMatricule,
            @Valid @RequestBody MembreDTO dto
    ) {
        membreService.addMemberAsAdmin(adminMatricule, dto);
    }

    @PostMapping(consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public void addMember(
            @Valid @RequestBody MembreDTO dto
    ) {
        membreService.addMember(dto);
    }

    @PatchMapping(
            value = "/{id}/active",
            consumes = "application/json",
            produces = "application/json"
    )
    public MembreDTO updateMemberActiveStatus(
            @PathVariable("id") int id,
            @Valid @RequestBody MemberActiveStatusRequest request
    ) {
        return membreMapper.toDTO(
                membreService.updateMemberActiveStatus(id, request.active())
        );
    }

    @PatchMapping(
            value = "/{id}/profile",
            consumes = "application/json",
            produces = "application/json"
    )
    public MembreDTO updateOwnProfile(
            @PathVariable("id") int id,
            @Valid @RequestBody MemberProfileUpdateRequest request
    ) {
        return membreMapper.toDTO(
                membreService.updateOwnProfile(
                        id,
                        request.firstName(),
                        request.lastName(),
                        request.email()
                )
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMember(
            @PathVariable("id") int id
    ) {
        membreService.deleteMember(id);
    }

    public record MemberActiveStatusRequest(
            @NotNull(message = "Le statut actif du membre est obligatoire.")
            Boolean active
    ) {
    }

    public record MemberProfileUpdateRequest(
            @NotBlank(message = "Le prénom du membre est obligatoire.")
            String firstName,

            @NotBlank(message = "Le nom du membre est obligatoire.")
            String lastName,

            @NotBlank(message = "L'email du membre est obligatoire.")
            @Email(message = "L'email du membre doit être valide.")
            String email
    ) {
    }
}