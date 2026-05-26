package be.angularpadelclub.Controler;

import be.angularpadelclub.DTO.MatchDTO;
import be.angularpadelclub.DTO.ReservationDTO;
import be.angularpadelclub.Mapper.MatchMapper;
import be.angularpadelclub.Mapper.ReservationMapper;
import be.angularpadelclub.Service.MatchService;
import be.angularpadelclub.Service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationMapper reservationMapper;
    private final MatchService matchService;
    private final MatchMapper matchMapper;

    @GetMapping(produces = "application/json")
    public List<ReservationDTO> findAll() {
        return reservationService.findAll();
    }

    @GetMapping(path = "/{id}", produces = "application/json")
    public ResponseEntity<ReservationDTO> reservation(
            @PathVariable int id
    ) {
        return ResponseEntity.of(
                reservationService.findById(id)
        );
    }

    @PostMapping(consumes = "application/json")
    public void addReservation(@RequestBody ReservationDTO reservationDTO) {
        reservationService.addReservation(reservationDTO);
    }


    @GetMapping(params = {"courtId", "date"}, produces = "application/json")
    public List<ReservationDTO> reservationsByCourtAndDate(
            @RequestParam int courtId,
            @RequestParam LocalDate date
    ) {
        return reservationMapper.toDTOList(
                reservationService.findByCourtAndDate(courtId, date)
        );
    }

}