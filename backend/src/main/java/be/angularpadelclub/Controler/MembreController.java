package be.angularpadelclub.Controler;

import be.angularpadelclub.DTO.MembreDTO;
import be.angularpadelclub.Mapper.MembreMapper;
import be.angularpadelclub.Service.MembreService;
import org.springframework.web.bind.annotation.*;

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
        return membreMapper.toDTOList(membreService.findAll());
    }

    @GetMapping("/search")
    public List<MembreDTO> findByNomAndPrenom(
            @RequestParam String nom,
            @RequestParam String prenom
    ) {
        return membreService
                .findByNomAndPrenom(nom, prenom)
                .stream().map(membreMapper::toDTO)
                .toList();
    }

    @GetMapping(value = "/{matricule}", produces = "application/json")
    public MembreDTO findById(@PathVariable String matricule) {
        return membreService.findByMatricule(matricule)
                .map(membreMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Member not found"));
    }

    @PostMapping(consumes = "application/json")
    public void addMember(@RequestBody MembreDTO dto) {
        membreService.addMember(dto);
    }

    @DeleteMapping("/{id}")
    public void deleteMember(@PathVariable int id) {
        membreService.deleteMember(id);
    }
}