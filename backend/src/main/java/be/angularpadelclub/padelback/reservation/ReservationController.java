package be.angularpadelclub.padelback.reservation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationMapper reservationMapper;

    @GetMapping(produces = "application/json")
    public List<ReservationDTO> reservations() {
        return reservationMapper.toDTOList(reservationService.findAll());
    }

    @GetMapping(path = "/{id}", produces = "application/json")
    public ResponseEntity<ReservationDTO> reservation(@PathVariable UUID id) {
        return ResponseEntity.of(
                reservationService.findById(id)
                        .map(reservationMapper::toDTO)
        );
    }

    @PostMapping(consumes = "application/json")
    public void addReservation(@RequestBody ReservationDTO reservationDTO) {
        ReservationEntity reservation = reservationMapper.toEntity(reservationDTO);
        reservationService.addReservation(reservation);
    }

    @DeleteMapping("/{id}")
    public void deleteReservation(@PathVariable UUID id) {
        reservationService.deleteReservation(id);
    }
}