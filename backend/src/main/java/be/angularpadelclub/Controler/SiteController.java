package be.angularpadelclub.Controler;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import be.angularpadelclub.Service.SiteService;
import be.angularpadelclub.DTO.SiteDTO;

import java.util.List;

@RestController
@RequestMapping("/api/sites")
@RequiredArgsConstructor
public class SiteController {

    private final SiteService siteService;

    @GetMapping
    public List<SiteDTO> getAllSites() {
        return siteService.getAllSites();
    }
}