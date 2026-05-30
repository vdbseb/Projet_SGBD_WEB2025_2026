package be.angularpadelclub.ServiceTest;

import be.angularpadelclub.Entity.MatchEntity;
import be.angularpadelclub.Entity.ReservationEntity;
import be.angularpadelclub.Enum.MatchStatus;
import be.angularpadelclub.Enum.ReservationStatus;
import be.angularpadelclub.Repository.ReservationRepository;
import be.angularpadelclub.Service.PaiementService;
import be.angularpadelclub.Service.ReservationCancellationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationCancellationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private PaiementService paiementService;

    private ReservationCancellationService reservationCancellationService;

    @BeforeEach
    void setUp() {
        reservationCancellationService = new ReservationCancellationService(
                reservationRepository,
                paiementService
        );
    }

    @Test
    @DisplayName("Annule une réservation pour raison club, annule le match et rembourse")
    void cancelReservationForClubReason_shouldCancelReservationCancelMatchAndRefund() {
        ReservationEntity reservation = reservation(1, ReservationStatus.VALIDEE);
        MatchEntity match = match(10, MatchStatus.OUVERT);
        reservation.setMatch(match);

        reservationCancellationService.cancelReservationForClubReason(reservation);

        assertEquals(ReservationStatus.ANNULEE, reservation.getStatut());
        assertEquals(MatchStatus.ANNULE, match.getStatut());

        verify(paiementService).rembourserPaiementsReservation(reservation);
        verify(reservationRepository).save(reservation);
    }

    @Test
    @DisplayName("Annule une réservation membre et rembourse aussi pour l'instant")
    void cancelReservationByMember_shouldCancelAndRefund() {
        ReservationEntity reservation = reservation(1, ReservationStatus.VALIDEE);
        MatchEntity match = match(10, MatchStatus.COMPLET);
        reservation.setMatch(match);

        reservationCancellationService.cancelReservationByMember(reservation);

        assertEquals(ReservationStatus.ANNULEE, reservation.getStatut());
        assertEquals(MatchStatus.ANNULE, match.getStatut());

        verify(paiementService).rembourserPaiementsReservation(reservation);
        verify(reservationRepository).save(reservation);
    }

    @Test
    @DisplayName("N'annule pas un match déjà terminé")
    void cancelReservationForClubReason_shouldNotCancelFinishedMatch() {
        ReservationEntity reservation = reservation(1, ReservationStatus.VALIDEE);
        MatchEntity match = match(10, MatchStatus.TERMINE);
        reservation.setMatch(match);

        reservationCancellationService.cancelReservationForClubReason(reservation);

        assertEquals(ReservationStatus.ANNULEE, reservation.getStatut());
        assertEquals(MatchStatus.TERMINE, match.getStatut());

        verify(paiementService).rembourserPaiementsReservation(reservation);
        verify(reservationRepository).save(reservation);
    }

    @Test
    @DisplayName("Annule une réservation sans match sans planter")
    void cancelReservationForClubReason_shouldCancelReservationWithoutMatch() {
        ReservationEntity reservation = reservation(1, ReservationStatus.VALIDEE);
        reservation.setMatch(null);

        reservationCancellationService.cancelReservationForClubReason(reservation);

        assertEquals(ReservationStatus.ANNULEE, reservation.getStatut());

        verify(paiementService).rembourserPaiementsReservation(reservation);
        verify(reservationRepository).save(reservation);
    }

    @Test
    @DisplayName("Ignore une réservation déjà annulée")
    void cancelReservationForClubReason_shouldIgnoreAlreadyCancelledReservation() {
        ReservationEntity reservation = reservation(1, ReservationStatus.ANNULEE);

        reservationCancellationService.cancelReservationForClubReason(reservation);

        verify(paiementService, never()).rembourserPaiementsReservation(any());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ignore une réservation terminée")
    void cancelReservationForClubReason_shouldIgnoreFinishedReservation() {
        ReservationEntity reservation = reservation(1, ReservationStatus.TERMINEE);

        reservationCancellationService.cancelReservationForClubReason(reservation);

        verify(paiementService, never()).rembourserPaiementsReservation(any());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ignore une réservation null")
    void cancelReservationForClubReason_shouldIgnoreNullReservation() {
        reservationCancellationService.cancelReservationForClubReason(null);

        verify(paiementService, never()).rembourserPaiementsReservation(any());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Annule toutes les réservations d'une liste")
    void cancelReservationsForClubReason_shouldCancelAllReservations() {
        ReservationEntity reservation1 = reservation(1, ReservationStatus.VALIDEE);
        ReservationEntity reservation2 = reservation(2, ReservationStatus.EN_ATTENTE_PAIEMENT);

        reservationCancellationService.cancelReservationsForClubReason(
                List.of(reservation1, reservation2)
        );

        assertEquals(ReservationStatus.ANNULEE, reservation1.getStatut());
        assertEquals(ReservationStatus.ANNULEE, reservation2.getStatut());

        verify(paiementService).rembourserPaiementsReservation(reservation1);
        verify(paiementService).rembourserPaiementsReservation(reservation2);
        verify(reservationRepository).save(reservation1);
        verify(reservationRepository).save(reservation2);
    }

    @Test
    @DisplayName("Ignore une liste null")
    void cancelReservationsForClubReason_shouldIgnoreNullList() {
        reservationCancellationService.cancelReservationsForClubReason(null);

        verify(paiementService, never()).rembourserPaiementsReservation(any());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ignore une liste vide")
    void cancelReservationsForClubReason_shouldIgnoreEmptyList() {
        reservationCancellationService.cancelReservationsForClubReason(List.of());

        verify(paiementService, never()).rembourserPaiementsReservation(any());
        verify(reservationRepository, never()).save(any());
    }

    private ReservationEntity reservation(Integer id, ReservationStatus status) {
        ReservationEntity reservation = new ReservationEntity();
        reservation.setId(id);
        reservation.setStatut(status);
        return reservation;
    }

    private MatchEntity match(Integer id, MatchStatus status) {
        MatchEntity match = new MatchEntity();
        match.setId(id);
        match.setStatut(status);
        return match;
    }
}