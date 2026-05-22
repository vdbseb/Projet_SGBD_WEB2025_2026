package be.angularpadelclub.Controler;

import be.angularpadelclub.DTO.CourtDTO;
import be.angularpadelclub.Service.CourtService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/terrains")
@RequiredArgsConstructor
public class CourtController {

    private final CourtService courtService;

    @GetMapping("/{siteId}")
    public List<CourtDTO> getCourtsBySiteId(
            @PathVariable Integer siteId
    ) {
        return courtService.getCourtsBySiteId(siteId);
    }
}