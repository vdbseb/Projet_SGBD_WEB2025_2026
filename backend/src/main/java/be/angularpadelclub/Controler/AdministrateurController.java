package be.angularpadelclub.Controler;

import be.angularpadelclub.DTO.AdministrateurDTO;
import be.angularpadelclub.Mapper.AdministrateurMapper;
import be.angularpadelclub.Service.AdministrateurService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/administrateurs")
@CrossOrigin(origins = "http://localhost:4200")
public class AdministrateurController {

    private final AdministrateurService administrateurService;
    private final AdministrateurMapper administrateurMapper;

    public AdministrateurController(
            AdministrateurService administrateurService,
            AdministrateurMapper administrateurMapper
    ) {
        this.administrateurService =
                administrateurService;
        this.administrateurMapper =
                administrateurMapper;
    }

    @GetMapping(produces = "application/json")
    public List<AdministrateurDTO> findAll() {
        return administrateurMapper.toDTOList(
                administrateurService.findAll()
        );
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public AdministrateurDTO findById(
            @PathVariable("id") Integer id
    ) {
        return administrateurMapper.toDTO(
                administrateurService.findById(id)
        );
    }

    @GetMapping(value = "/matricule/{matricule}", produces = "application/json")
    public AdministrateurDTO findByMatricule(
            @PathVariable("matricule") String matricule
    ) {
        return administrateurMapper.toDTO(
                administrateurService
                        .findByMatricule(matricule)
        );
    }

    @GetMapping(value = "/type/{typeAdmin}", produces = "application/json")
    public List<AdministrateurDTO> findByTypeAdmin(
            @PathVariable("typeAdmin") String typeAdmin
    ) {
        return administrateurMapper.toDTOList(
                administrateurService
                        .findByTypeAdmin(typeAdmin)
        );
    }

    @GetMapping(value = "/site/{siteId}", produces = "application/json")
    public List<AdministrateurDTO> findBySiteId(
            @PathVariable("siteId") Integer siteId
    ) {
        return administrateurMapper.toDTOList(
                administrateurService
                        .findBySiteId(siteId)
        );
    }
}