package be.angularpadelclub.Controler;

import be.angularpadelclub.DTO.PenaliteDTO;
import be.angularpadelclub.Service.PenaliteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/penalites")
@CrossOrigin(origins = "http://localhost:4200")
public class PenaliteController {

    private final PenaliteService penaliteService;

    public PenaliteController(
            PenaliteService penaliteService
    ) {
        this.penaliteService = penaliteService;
    }

    @GetMapping(
            value = "/member/{memberId}/active",
            produces = "application/json"
    )
    public List<PenaliteDTO> findActivePenaltiesForMember(
            @PathVariable("memberId") Integer memberId
    ) {
        return penaliteService.findActivePenaltiesForMember(memberId);
    }
}