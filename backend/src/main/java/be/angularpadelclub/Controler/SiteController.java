package be.angularpadelclub.Controler;

import be.angularpadelclub.DTO.SiteDTO;
import be.angularpadelclub.Service.SiteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sites")
@CrossOrigin(origins = "http://localhost:4200")
public class SiteController {

    private final SiteService siteService;

    public SiteController(
            SiteService siteService
    ) {
        this.siteService = siteService;
    }

    @GetMapping(produces = "application/json")
    public List<SiteDTO> getAllSites() {
        return siteService.getAllSites();
    }

    @GetMapping(
            value = "/{id}",
            produces = "application/json"
    )
    public SiteDTO getSiteById(
            @PathVariable("id") int id
    ) {
        return siteService.getSiteById(id);
    }

    @PostMapping(
            consumes = "application/json",
            produces = "application/json"
    )
    public SiteDTO createSite(
            @RequestBody SiteDTO dto
    ) {
        return siteService.createSite(dto);
    }

    @PutMapping(
            value = "/{id}",
            consumes = "application/json",
            produces = "application/json"
    )
    public SiteDTO updateSite(
            @PathVariable("id") int id,
            @RequestBody SiteDTO dto
    ) {
        return siteService.updateSite(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteSite(
            @PathVariable("id") int id
    ) {
        siteService.deleteSite(id);
    }
}