package be.angularpadelclub.Controler;

import be.angularpadelclub.DTO.HoraireSiteDTO;
import be.angularpadelclub.Mapper.HoraireSiteMapper;
import be.angularpadelclub.Service.HoraireSiteService;
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

    @GetMapping
    public List<HoraireSiteDTO> findAll() {
        return horaireSiteMapper.toDTOList(horaireSiteService.findAll());
    }

    @GetMapping("/{id}")
    public HoraireSiteDTO findById(@PathVariable Integer id) {
        return horaireSiteMapper.toDTO(horaireSiteService.findById(id));
    }

    @GetMapping("/site/{siteId}")
    public List<HoraireSiteDTO> findBySiteId(@PathVariable Integer siteId) {
        return horaireSiteMapper.toDTOList(horaireSiteService.findBySiteId(siteId));
    }

    @GetMapping("/site/{siteId}/annee/{annee}")
    public HoraireSiteDTO findBySiteIdAndAnnee(
            @PathVariable Integer siteId,
            @PathVariable int annee
    ) {
        return horaireSiteMapper.toDTO(
                horaireSiteService.findBySiteIdAndAnnee(siteId, annee)
        );
    }

    @PostMapping
    public HoraireSiteDTO create(@RequestBody HoraireSiteDTO dto) {
        return horaireSiteMapper.toDTO(horaireSiteService.create(dto));
    }

    @PutMapping("/{id}")
    public HoraireSiteDTO update(
            @PathVariable Integer id,
            @RequestBody HoraireSiteDTO dto
    ) {
        return horaireSiteMapper.toDTO(horaireSiteService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        horaireSiteService.delete(id);
    }
}