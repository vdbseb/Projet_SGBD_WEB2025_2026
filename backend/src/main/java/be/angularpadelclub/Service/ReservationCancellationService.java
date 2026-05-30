package be.angularpadelclub.Service;

import be.angularpadelclub.Entity.ReservationEntity;
import be.angularpadelclub.Enum.MatchStatus;
import be.angularpadelclub.Enum.ReservationStatus;
import be.angularpadelclub.Repository.ReservationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservationCancellationService {

    private final ReservationRepository reservationRepository;
    private final PaiementService paiementService;

    public ReservationCancellationService(
            ReservationRepository reservationRepository,
            PaiementService paiementService
    ) {
        this.reservationRepository = reservationRepository;
        this.paiementService = paiementService;
    }

    @Transactional
    public void cancelReservationByMember(
            ReservationEntity reservation
    ) {
        cancelReservation(
                reservation,
                true
        );
    }

    @Transactional
    public void cancelReservationForClubReason(
            ReservationEntity reservation
    ) {
        cancelReservation(
                reservation,
                true
        );
    }

    @Transactional
    public void cancelReservationsForClubReason(
            List<ReservationEntity> reservations
    ) {
        if (reservations == null || reservations.isEmpty()) {
            return;
        }

        for (ReservationEntity reservation : reservations) {
            cancelReservationForClubReason(reservation);
        }
    }

    private void cancelReservation(
            ReservationEntity reservation,
            boolean refundPayments
    ) {
        if (!canBeCancelled(reservation)) {
            return;
        }

        reservation.setStatut(ReservationStatus.ANNULEE);

        if (reservation.getMatch() != null
                && reservation.getMatch().getStatut() != MatchStatus.TERMINE) {
            reservation.getMatch().setStatut(MatchStatus.ANNULE);
        }

        if (refundPayments) {
            paiementService.rembourserPaiementsReservation(reservation);
        }

        reservationRepository.save(reservation);
    }

    private boolean canBeCancelled(
            ReservationEntity reservation
    ) {
        return reservation != null
                && reservation.getStatut() != ReservationStatus.ANNULEE
                && reservation.getStatut() != ReservationStatus.TERMINEE;
    }
}