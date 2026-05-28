package be.angularpadelclub.Controler;

import be.angularpadelclub.DTO.MemberWalletDTO;
import be.angularpadelclub.DTO.PaiementDTO;
import be.angularpadelclub.Service.PaiementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/paiements")
@CrossOrigin(origins = "http://localhost:4200")
public class PaiementController {

    private final PaiementService paiementService;

    public PaiementController(PaiementService paiementService) {
        this.paiementService = paiementService;
    }

    @GetMapping(produces = "application/json")
    public List<PaiementDTO> findAll() {
        return paiementService.findAll();
    }

    @GetMapping(path = "/{id}", produces = "application/json")
    public ResponseEntity<PaiementDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.of(paiementService.findById(id));
    }

    @GetMapping(path = "/reservation/{reservationId}", produces = "application/json")
    public List<PaiementDTO> findByReservation(
            @PathVariable Integer reservationId
    ) {
        return paiementService.findByReservation(reservationId);
    }

    @GetMapping(path = "/member/{memberId}/wallet", produces = "application/json")
    public MemberWalletDTO getMemberWallet(@PathVariable Integer memberId) {
        return paiementService.getWallet(memberId);
    }

    @PostMapping(path = "/reservation/{reservationId}/initier", produces = "application/json")
    public PaiementDTO initierPaiement(@PathVariable Integer reservationId) {
        return paiementService.initierPaiement(reservationId);
    }

    @PostMapping(path = "/participation/{participationId}/initier", produces = "application/json")
    public PaiementDTO initierPaiementPourParticipation(
            @PathVariable Integer participationId
    ) {
        return paiementService.initierPaiementPourParticipation(participationId);
    }

    @PatchMapping(path = "/{id}/confirmer", produces = "application/json")
    public PaiementDTO confirmerPaiement(@PathVariable Integer id) {
        return paiementService.confirmerPaiement(id);
    }

    @PatchMapping(path = "/{id}/refuser", produces = "application/json")
    public PaiementDTO refuserPaiement(@PathVariable Integer id) {
        return paiementService.refuserPaiement(id);
    }

    @PatchMapping(path = "/{id}/annuler", produces = "application/json")
    public PaiementDTO annulerPaiement(@PathVariable Integer id) {
        return paiementService.annulerPaiement(id);
    }

    @PatchMapping(path = "/{id}/rembourser", produces = "application/json")
    public PaiementDTO rembourserPaiement(@PathVariable Integer id) {
        return paiementService.rembourserPaiement(id);
    }
}
