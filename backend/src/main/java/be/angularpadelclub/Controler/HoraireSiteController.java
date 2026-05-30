package be.angularpadelclub.Controler;

import be.angularpadelclub.DTO.HoraireSiteDTO;
import be.angularpadelclub.Mapper.HoraireSiteMapper;
import be.angularpadelclub.Service.HoraireSiteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horaires-sites")
@CrossOrigin(origins = "http://localhost:4200")
public class HoraireSiteController {

    private final HoraireSiteService horaireSiteService;
    private final HoraireSiteMapper horaireSiteMapper;

    public HoraireSiteController(
            HoraireSiteService horaireSiteService,
            HoraireSiteMapper horaireSiteMapper
    ) {
        this.horaireSiteService = horaireSiteService;
        this.horaireSiteMapper = horaireSiteMapper;
    }

    @GetMapping(produces = "application/json")
    public List<HoraireSiteDTO> findAll() {
        return horaireSiteMapper.toDTOList(
                horaireSiteService.findAll()
        );
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public HoraireSiteDTO findById(
            @PathVariable("id") Integer id
    ) {
        return horaireSiteMapper.toDTO(
                horaireSiteService.findById(id)
        );
    }

    @GetMapping(value = "/site/{siteId}", produces = "application/json")
    public List<HoraireSiteDTO> findBySiteId(
            @PathVariable("siteId") Integer siteId
    ) {
        return horaireSiteMapper.toDTOList(
                horaireSiteService.findBySiteId(siteId)
        );
    }

    @GetMapping(
            value = "/site/{siteId}/annee/{annee}",
            produces = "application/json"
    )
    public HoraireSiteDTO findBySiteIdAndAnnee(
            @PathVariable("siteId") Integer siteId,
            @PathVariable("annee") int annee
    ) {
        return horaireSiteMapper.toDTO(
                horaireSiteService.findBySiteIdAndAnnee(siteId, annee)
        );
    }

    @PostMapping(
            consumes = "application/json",
            produces = "application/json"
    )
    @ResponseStatus(HttpStatus.CREATED)
    public HoraireSiteDTO create(
            @Valid @RequestBody HoraireSiteDTO dto
    ) {
        return horaireSiteMapper.toDTO(
                horaireSiteService.create(dto)
        );
    }

    @PutMapping(
            value = "/{id}",
            consumes = "application/json",
            produces = "application/json"
    )
    public HoraireSiteDTO update(
            @PathVariable("id") Integer id,
            @Valid  @RequestBody HoraireSiteDTO dto
    ) {
        return horaireSiteMapper.toDTO(
                horaireSiteService.update(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable("id") Integer id
    ) {
        horaireSiteService.delete(id);
    }
}