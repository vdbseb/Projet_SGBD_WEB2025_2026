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

    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    @GetMapping
    public List<SiteDTO> getAllSites() {
        return siteService.getAllSites();
    }

    @GetMapping("/{id}")
    public SiteDTO getSiteById(@PathVariable int id) {
        return siteService.getSiteById(id);
    }

    @PostMapping
    public SiteDTO createSite(@RequestBody SiteDTO dto) {
        return siteService.createSite(dto);
    }

    @PutMapping("/{id}")
    public SiteDTO updateSite(
            @PathVariable int id,
            @RequestBody SiteDTO dto
    ) {
        return siteService.updateSite(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteSite(@PathVariable int id) {
        siteService.deleteSite(id);
    }
}