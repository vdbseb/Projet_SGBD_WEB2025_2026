package be.angularpadelclub.Controler;

import be.angularpadelclub.DTO.ReservationDTO;
import be.angularpadelclub.Mapper.ReservationMapper;
import be.angularpadelclub.Service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "http://localhost:4200")
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
            @PathVariable("id") int id
    ) {
        return ResponseEntity.of(
                reservationService.findById(id)
        );
    }

    @PostMapping(consumes = "application/json")
    public void addReservation(
            @RequestBody ReservationDTO reservationDTO
    ) {
        reservationService.addReservation(reservationDTO);
    }

    @GetMapping(
            params = {"courtId", "date"},
            produces = "application/json"
    )
    public List<ReservationDTO> reservationsByCourtAndDate(
            @RequestParam("courtId") int courtId,
            @RequestParam("date") LocalDate date
    ) {
        return reservationMapper.toDTOList(
                reservationService.findByCourtAndDate(courtId, date)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable("id") int id,
            @RequestParam(value = "memberId", required = false) Integer memberId
    ) {
        if (memberId == null) {
            reservationService.cancelReservationByAdmin(id);
        } else {
            reservationService.cancelReservation(
                    id,
                    memberId
            );
        }

        return ResponseEntity.noContent().build();
    }
}