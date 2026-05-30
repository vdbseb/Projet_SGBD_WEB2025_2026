package be.angularpadelclub.Controler;

import be.angularpadelclub.DTO.ReservationDTO;
import be.angularpadelclub.Mapper.ReservationMapper;
import be.angularpadelclub.Service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    public ReservationDTO reservation(
            @PathVariable("id") int id
    ) {
        return reservationService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Réservation introuvable avec l'id : " + id
                ));
    }

    @PostMapping(consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public void addReservation(
            @Valid  @RequestBody ReservationDTO reservationDTO
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
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelReservation(
            @PathVariable("id") int id,
            @RequestParam(value = "memberId", required = false) Integer memberId
    ) {
        if (memberId == null) {
            reservationService.cancelReservationByAdmin(id);
            return;
        }

        reservationService.cancelReservation(id, memberId);
    }
}