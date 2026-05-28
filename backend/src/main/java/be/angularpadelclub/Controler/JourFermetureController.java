package be.angularpadelclub.Controler;

import be.angularpadelclub.DTO.JourFermetureDTO;
import be.angularpadelclub.Mapper.JourFermetureMapper;
import be.angularpadelclub.Service.JourFermetureService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jours-fermeture")
@CrossOrigin(origins = "http://localhost:4200")
public class JourFermetureController {

    private final JourFermetureService jourFermetureService;
    private final JourFermetureMapper jourFermetureMapper;

    public JourFermetureController(
            JourFermetureService jourFermetureService,
            JourFermetureMapper jourFermetureMapper
    ) {
        this.jourFermetureService = jourFermetureService;
        this.jourFermetureMapper = jourFermetureMapper;
    }

    @GetMapping
    public List<JourFermetureDTO> findAll() {
        return jourFermetureMapper.toDTOList(jourFermetureService.findAll());
    }

    @GetMapping("/{id}")
    public JourFermetureDTO findById(@PathVariable Integer id) {
        return jourFermetureMapper.toDTO(jourFermetureService.findById(id));
    }

    @GetMapping("/site/{siteId}")
    public List<JourFermetureDTO> findBySiteId(@PathVariable Integer siteId) {
        return jourFermetureMapper.toDTOList(jourFermetureService.findBySiteId(siteId));
    }

    @GetMapping("/globales")
    public List<JourFermetureDTO> findGlobalClosures() {
        return jourFermetureMapper.toDTOList(jourFermetureService.findGlobalClosures());
    }

    @PostMapping
    public JourFermetureDTO create(@RequestBody JourFermetureDTO dto) {
        return jourFermetureMapper.toDTO(jourFermetureService.create(dto));
    }

    @PutMapping("/{id}")
    public JourFermetureDTO update(
            @PathVariable Integer id,
            @RequestBody JourFermetureDTO dto
    ) {
        return jourFermetureMapper.toDTO(jourFermetureService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        jourFermetureService.delete(id);
    }
}