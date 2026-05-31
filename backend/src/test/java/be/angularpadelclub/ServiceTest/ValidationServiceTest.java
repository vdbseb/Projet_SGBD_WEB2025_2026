package be.angularpadelclub.ServiceTest;

import be.angularpadelclub.Entity.CourtEntity;
import be.angularpadelclub.Entity.HoraireSiteEntity;
import be.angularpadelclub.Entity.MatchEntity;
import be.angularpadelclub.Entity.MembreEntity;
import be.angularpadelclub.Entity.ReservationEntity;
import be.angularpadelclub.Entity.SiteEntity;
import be.angularpadelclub.Enum.DetteStatut;
import be.angularpadelclub.Enum.MatchStatus;
import be.angularpadelclub.Enum.ReservationStatus;
import be.angularpadelclub.Repository.DetteMembreRepository;
import be.angularpadelclub.Repository.PenaliteRepository;
import be.angularpadelclub.Repository.ReservationRepository;
import be.angularpadelclub.Service.HoraireSiteService;
import be.angularpadelclub.Service.JourFermetureService;
import be.angularpadelclub.Service.ReservationValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationValidationServiceTest {

    @Mock
    private PenaliteRepository penaliteRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private DetteMembreRepository detteMembreRepository;

    @Mock
    private JourFermetureService jourFermetureService;

    @Mock
    private HoraireSiteService horaireSiteService;

    private ReservationValidationService service;

    @BeforeEach
    void setUp() {
        service = new ReservationValidationService(
                penaliteRepository,
                reservationRepository,
                detteMembreRepository,
                jourFermetureService,
                horaireSiteService
        );
    }

    @Test
    @DisplayName("accepte une reservation valide dans les horaires et sans conflit")
    void validateReservationPossible_shouldReturnScheduleWhenEverythingIsValid() {
        SiteEntity site = site(1, true);
        CourtEntity court = court(10, site, true, false);
        MembreEntity member = member(20, "G0001", true, null);
        LocalDate date = LocalDate.now().plusDays(2);
        HoraireSiteEntity schedule = schedule(site);

        when(horaireSiteService.findBySiteIdAndAnnee(1, date.getYear()))
                .thenReturn(schedule);
        when(reservationRepository.findByCourtAndDate(court, date))
                .thenReturn(List.of());

        HoraireSiteEntity result =
                service.validateReservationPossible(court, member, date, LocalTime.of(10, 0));

        assertSame(schedule, result);
    }

    @Test
    @DisplayName("refuse un membre inactif")
    void validateReservationPossible_shouldRejectInactiveMember() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.validateReservationPossible(
                        court(10, site(1, true), true, false),
                        member(20, "G0001", false, null),
                        LocalDate.now().plusDays(1),
                        LocalTime.of(10, 0)
                )
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    @DisplayName("refuse un membre avec penalite active")
    void validateReservationPossible_shouldRejectMemberWithActivePenalty() {
        MembreEntity member = member(20, "G0001", true, null);

        when(penaliteRepository.existsByMembre_IdAndActiveTrueAndDateFinGreaterThanEqual(
                20,
                LocalDate.now()
        )).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.validateReservationPossible(
                        court(10, site(1, true), true, false),
                        member,
                        LocalDate.now().plusDays(1),
                        LocalTime.of(10, 0)
                )
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    @DisplayName("refuse un membre avec dette ouverte")
    void validateReservationPossible_shouldRejectMemberWithOpenDebt() {
        MembreEntity member = member(20, "G0001", true, null);

        when(detteMembreRepository.existsByMembre_IdAndStatut(20, DetteStatut.OUVERTE))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.validateReservationPossible(
                        court(10, site(1, true), true, false),
                        member,
                        LocalDate.now().plusDays(1),
                        LocalTime.of(10, 0)
                )
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    @DisplayName("refuse une date passee")
    void validateReservationPossible_shouldRejectPastDate() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.validateReservationPossible(
                        court(10, site(1, true), true, false),
                        member(20, "G0001", true, null),
                        LocalDate.now().minusDays(1),
                        LocalTime.of(10, 0)
                )
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    @DisplayName("refuse un membre site sur un autre site")
    void validateReservationPossible_shouldRejectSiteMemberOnAnotherSite() {
        SiteEntity memberSite = site(1, true);
        SiteEntity courtSite = site(2, true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.validateReservationPossible(
                        court(10, courtSite, true, false),
                        member(20, "S0001", true, memberSite),
                        LocalDate.now().plusDays(1),
                        LocalTime.of(10, 0)
                )
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    @DisplayName("refuse un terrain en maintenance")
    void validateReservationPossible_shouldRejectCourtInMaintenance() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.validateReservationPossible(
                        court(10, site(1, true), true, true),
                        member(20, "G0001", true, null),
                        LocalDate.now().plusDays(1),
                        LocalTime.of(10, 0)
                )
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    @DisplayName("refuse un site ferme globalement")
    void validateReservationPossible_shouldRejectGlobalClosingDay() {
        SiteEntity site = site(1, true);
        CourtEntity court = court(10, site, true, false);
        MembreEntity member = member(20, "G0001", true, null);
        LocalDate date = LocalDate.now().plusDays(1);

        when(jourFermetureService.existsGlobalByDate(date)).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.validateReservationPossible(court, member, date, LocalTime.of(10, 0))
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    @DisplayName("refuse un creneau qui depasse la fermeture")
    void validateReservationPossible_shouldRejectSlotAfterClosingTime() {
        SiteEntity site = site(1, true);
        CourtEntity court = court(10, site, true, false);
        MembreEntity member = member(20, "G0001", true, null);
        LocalDate date = LocalDate.now().plusDays(1);

        when(horaireSiteService.findBySiteIdAndAnnee(1, date.getYear()))
                .thenReturn(schedule(site));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.validateReservationPossible(court, member, date, LocalTime.of(21, 0))
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    @DisplayName("refuse un chevauchement incluant la pause")
    void validateReservationPossible_shouldRejectOverlapWithPause() {
        SiteEntity site = site(1, true);
        CourtEntity court = court(10, site, true, false);
        MembreEntity member = member(20, "G0001", true, null);
        LocalDate date = LocalDate.now().plusDays(1);
        HoraireSiteEntity schedule = schedule(site);
        ReservationEntity existing = reservation(court, date, LocalTime.of(10, 0), LocalTime.of(11, 30));

        when(horaireSiteService.findBySiteIdAndAnnee(1, date.getYear()))
                .thenReturn(schedule);
        when(reservationRepository.findByCourtAndDate(court, date))
                .thenReturn(List.of(existing));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.validateReservationPossible(court, member, date, LocalTime.of(11, 40))
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    @DisplayName("ignore une reservation annulee dans le controle de chevauchement")
    void validateReservationPossible_shouldIgnoreCancelledReservation() {
        SiteEntity site = site(1, true);
        CourtEntity court = court(10, site, true, false);
        MembreEntity member = member(20, "G0001", true, null);
        LocalDate date = LocalDate.now().plusDays(1);
        HoraireSiteEntity schedule = schedule(site);
        ReservationEntity existing = reservation(court, date, LocalTime.of(10, 0), LocalTime.of(11, 30));
        existing.setStatut(ReservationStatus.ANNULEE);

        when(horaireSiteService.findBySiteIdAndAnnee(1, date.getYear()))
                .thenReturn(schedule);
        when(reservationRepository.findByCourtAndDate(court, date))
                .thenReturn(List.of(existing));

        HoraireSiteEntity result =
                service.validateReservationPossible(court, member, date, LocalTime.of(10, 30));

        assertSame(schedule, result);
    }

    @Test
    @DisplayName("refuse plus de quatre joueurs")
    void validateParticipants_shouldRejectTooManyPlayers() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.validateParticipants(List.of("G0002", "S0001", "L0001", "L0002"))
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    @DisplayName("refuse les doublons de participants apres normalisation")
    void validateParticipants_shouldRejectDuplicateParticipantsIgnoringCaseAndSpaces() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.validateParticipants(List.of(" g0002 ", "G0002", "L0001"))
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    @DisplayName("refuse un match prive incomplet")
    void validateParticipants_shouldRejectIncompletePrivateMatch() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.validateParticipants(List.of("G0002"))
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    private SiteEntity site(Integer id, boolean active) {
        SiteEntity site = new SiteEntity();
        site.setId(id);
        site.setActif(active);
        return site;
    }

    private CourtEntity court(Integer id, SiteEntity site, boolean active, boolean maintenance) {
        CourtEntity court = new CourtEntity();
        court.setId(id);
        court.setSite(site);
        court.setActif(active);
        court.setMaintenance(maintenance);
        return court;
    }

    private MembreEntity member(Integer id, String matricule, boolean active, SiteEntity site) {
        MembreEntity member = new MembreEntity();
        member.setId(id);
        member.setMatricule(matricule);
        member.setActif(active);
        member.setSite(site);
        return member;
    }

    private HoraireSiteEntity schedule(SiteEntity site) {
        HoraireSiteEntity schedule = new HoraireSiteEntity();
        schedule.setSite(site);
        schedule.setAnnee(LocalDate.now().getYear());
        schedule.setHeure_debut(LocalTime.of(8, 0));
        schedule.setHeure_fin(LocalTime.of(22, 0));
        schedule.setDuree_match_minutes(90);
        schedule.setPause_minutes(15);
        return schedule;
    }

    private ReservationEntity reservation(
            CourtEntity court,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime
    ) {
        ReservationEntity reservation = new ReservationEntity();
        reservation.setCourt(court);
        reservation.setDate(date);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        reservation.setStatut(ReservationStatus.EN_ATTENTE_PAIEMENT);
        return reservation;
    }
}
