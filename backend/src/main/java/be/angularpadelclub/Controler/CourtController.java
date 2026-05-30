package be.angularpadelclub.Controler;

import be.angularpadelclub.DTO.CourtDTO;
import be.angularpadelclub.Service.CourtService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courts")
@CrossOrigin(origins = "http://localhost:4200")
public class CourtController {

    private final CourtService courtService;

    public CourtController(
            CourtService courtService
    ) {
        this.courtService = courtService;
    }

    @GetMapping(produces = "application/json")
    public List<CourtDTO> getAllCourts() {
        return courtService.getAllCourts();
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public CourtDTO getCourtById(
            @PathVariable("id") int id
    ) {
        return courtService.getCourtById(id);
    }

    @PostMapping(
            consumes = "application/json",
            produces = "application/json"
    )
    @ResponseStatus(HttpStatus.CREATED)
    public CourtDTO createCourt(
            @RequestBody CourtDTO dto
    ) {
        return courtService.createCourt(dto);
    }

    @PutMapping(
            value = "/{id}",
            consumes = "application/json",
            produces = "application/json"
    )
    public CourtDTO updateCourt(
            @PathVariable("id") int id,
            @RequestBody CourtDTO dto
    ) {
        return courtService.updateCourt(id, dto);
    }

    @PatchMapping(
            value = "/{id}/maintenance",
            produces = "application/json"
    )
    public CourtDTO setMaintenance(
            @PathVariable("id") int id,
            @RequestParam("maintenance") boolean maintenance
    ) {
        return courtService.setMaintenance(id, maintenance);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCourt(
            @PathVariable("id") int id
    ) {
        courtService.deleteCourt(id);
    }
}