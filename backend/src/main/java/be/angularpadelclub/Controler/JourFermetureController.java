package be.angularpadelclub.Controler;

import be.angularpadelclub.DTO.JourFermetureDTO;
import be.angularpadelclub.Mapper.JourFermetureMapper;
import be.angularpadelclub.Service.JourFermetureService;
import org.springframework.http.HttpStatus;
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

    @GetMapping(produces = "application/json")
    public List<JourFermetureDTO> findAll() {
        return jourFermetureMapper.toDTOList(
                jourFermetureService.findAll()
        );
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public JourFermetureDTO findById(
            @PathVariable("id") Integer id
    ) {
        return jourFermetureMapper.toDTO(
                jourFermetureService.findById(id)
        );
    }

    @GetMapping(value = "/site/{siteId}", produces = "application/json")
    public List<JourFermetureDTO> findBySiteId(
            @PathVariable("siteId") Integer siteId
    ) {
        return jourFermetureMapper.toDTOList(
                jourFermetureService.findBySiteId(siteId)
        );
    }

    @GetMapping(value = "/globales", produces = "application/json")
    public List<JourFermetureDTO> findGlobalClosures() {
        return jourFermetureMapper.toDTOList(
                jourFermetureService.findGlobalClosures()
        );
    }

    @PostMapping(
            consumes = "application/json",
            produces = "application/json"
    )
    @ResponseStatus(HttpStatus.CREATED)
    public JourFermetureDTO create(
            @RequestBody JourFermetureDTO dto
    ) {
        return jourFermetureMapper.toDTO(
                jourFermetureService.create(dto)
        );
    }

    @PutMapping(
            value = "/{id}",
            consumes = "application/json",
            produces = "application/json"
    )
    public JourFermetureDTO update(
            @PathVariable("id") Integer id,
            @RequestBody JourFermetureDTO dto
    ) {
        return jourFermetureMapper.toDTO(
                jourFermetureService.update(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable("id") Integer id
    ) {
        jourFermetureService.delete(id);
    }
}