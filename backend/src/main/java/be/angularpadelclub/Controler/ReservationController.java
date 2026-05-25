package be.angularpadelclub.Controler;

import be.angularpadelclub.DTO.ReservationDTO;
import be.angularpadelclub.DTO.ReservationDetailDTO;
import be.angularpadelclub.Mapper.ReservationDetailMapper;
import be.angularpadelclub.Mapper.ReservationMapper;
import be.angularpadelclub.Service.ReservationDetailService;
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

    @DeleteMapping("/{id}")
    public void deleteReservation(@PathVariable int id) {
        reservationService.deleteReservation(id);
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