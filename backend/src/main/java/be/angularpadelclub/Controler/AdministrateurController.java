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

    @GetMapping
    public List<AdministrateurDTO> findAll() {
        return administrateurMapper.toDTOList(
                administrateurService.findAll()
        );
    }

    @GetMapping("/{id}")
    public AdministrateurDTO findById(
            @PathVariable Integer id
    ) {
        return administrateurMapper.toDTO(
                administrateurService.findById(id)
        );
    }

    @GetMapping("/matricule/{matricule}")
    public AdministrateurDTO findByMatricule(
            @PathVariable String matricule
    ) {
        return administrateurMapper.toDTO(
                administrateurService
                        .findByMatricule(matricule)
        );
    }

    @GetMapping("/type/{typeAdmin}")
    public List<AdministrateurDTO> findByTypeAdmin(
            @PathVariable String typeAdmin
    ) {
        return administrateurMapper.toDTOList(
                administrateurService
                        .findByTypeAdmin(typeAdmin)
        );
    }

    @GetMapping("/site/{siteId}")
    public List<AdministrateurDTO> findBySiteId(
            @PathVariable Integer siteId
    ) {
        return administrateurMapper.toDTOList(
                administrateurService
                        .findBySiteId(siteId)
        );
    }
}