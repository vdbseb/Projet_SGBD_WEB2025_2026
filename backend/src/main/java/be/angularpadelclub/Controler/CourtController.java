package be.angularpadelclub.Controler;

import be.angularpadelclub.DTO.CourtDTO;
import be.angularpadelclub.Service.CourtService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/courts")
@CrossOrigin(origins = "http://localhost:4200")
public class CourtController {

    private final CourtService courtService;

    public CourtController(CourtService courtService) {
        this.courtService = courtService;
    }

    @GetMapping
    public List<CourtDTO> getAllCourts() {
        return courtService.getAllCourts();
    }

    @GetMapping("/{id}")
    public CourtDTO getCourtById(@PathVariable int id) {
        return courtService.getCourtById(id);
    }

    @PostMapping
    public CourtDTO createCourt(@RequestBody CourtDTO dto) {
        return courtService.createCourt(dto);
    }

    @DeleteMapping("/{id}")
    public void deleteCourt(@PathVariable int id) {
        courtService.deleteCourt(id);
    }
}