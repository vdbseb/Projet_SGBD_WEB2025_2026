package be.angularpadelclub.ServiceTest;

import be.angularpadelclub.DTO.PenaliteDTO;
import be.angularpadelclub.Entity.CourtEntity;
import be.angularpadelclub.Entity.MatchEntity;
import be.angularpadelclub.Entity.MembreEntity;
import be.angularpadelclub.Entity.PenaliteEntity;
import be.angularpadelclub.Entity.ReservationEntity;
import be.angularpadelclub.Entity.SiteEntity;
import be.angularpadelclub.Repository.PenaliteRepository;
import be.angularpadelclub.Service.PenaliteService;
import be.angularpadelclub.Service.ReferenceLookupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PenaliteServiceTest {

    private PenaliteRepository penaliteRepository;
    private ReferenceLookupService referenceLookupService;
    private PenaliteService service;

    @BeforeEach
    void setUp() {
        penaliteRepository = mock(PenaliteRepository.class);
        referenceLookupService = mock(ReferenceLookupService.class);

        service = new PenaliteService(
                penaliteRepository,
                referenceLookupService
        );
    }

    @Test
    void findActivePenaltiesForMember_checksMemberAndMapsPenalty() {
        MembreEntity membre = new MembreEntity();
        membre.setId(1);

        SiteEntity site = new SiteEntity();
        site.setNom("Brussels Padel");

        CourtEntity court = new CourtEntity();
        court.setNom("Terrain 1");
        court.setSite(site);

        ReservationEntity reservation = new ReservationEntity();
        reservation.setCourt(court);

        MatchEntity match = new MatchEntity();
        match.setId(10);
        match.setReservation(reservation);
        match.setDateMatch(LocalDate.of(2026, 7, 10));
        match.setHeureDebut(LocalTime.of(10, 0));
        match.setHeureFin(LocalTime.of(11, 30));

        PenaliteEntity penalite = new PenaliteEntity();
        penalite.setId(5);
        penalite.setMembre(membre);
        penalite.setMatch(match);
        penalite.setDateDebut(LocalDate.now().minusDays(1));
        penalite.setDateFin(LocalDate.now().plusDays(6));
        penalite.setRaison("Match privé incomplet");
        penalite.setActive(true);

        when(referenceLookupService.findMembreOrThrow(1)).thenReturn(membre);
        when(penaliteRepository.findByMembre_IdAndActiveTrueAndDateFinGreaterThanEqualOrderByDateFinAsc(
                1,
                LocalDate.now()
        )).thenReturn(List.of(penalite));

        List<PenaliteDTO> result = service.findActivePenaltiesForMember(1);

        assertEquals(1, result.size());
        assertEquals(5, result.get(0).id());
        assertEquals(1, result.get(0).membreId());
        assertEquals(10, result.get(0).matchId());
        assertEquals("Match privé incomplet", result.get(0).raison());
        assertEquals("Terrain 1", result.get(0).courtName());
        assertEquals("Brussels Padel", result.get(0).siteName());
        verify(referenceLookupService).findMembreOrThrow(1);
    }

    @Test
    void findActivePenaltiesForMember_returnsEmptyListWhenNoPenalty() {
        MembreEntity membre = new MembreEntity();

        when(referenceLookupService.findMembreOrThrow(1)).thenReturn(membre);
        when(penaliteRepository.findByMembre_IdAndActiveTrueAndDateFinGreaterThanEqualOrderByDateFinAsc(
                1,
                LocalDate.now()
        )).thenReturn(List.of());

        List<PenaliteDTO> result = service.findActivePenaltiesForMember(1);

        assertEquals(0, result.size());
        verify(referenceLookupService).findMembreOrThrow(1);
    }
}