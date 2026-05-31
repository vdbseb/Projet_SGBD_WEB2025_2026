package be.angularpadelclub.ServiceTest;

import be.angularpadelclub.DTO.CourtDTO;
import be.angularpadelclub.Entity.CourtEntity;
import be.angularpadelclub.Entity.ReservationEntity;
import be.angularpadelclub.Entity.SiteEntity;
import be.angularpadelclub.Mapper.CourtMapper;
import be.angularpadelclub.Repository.CourtRepository;
import be.angularpadelclub.Repository.ReservationRepository;
import be.angularpadelclub.Service.CourtService;
import be.angularpadelclub.Service.ReferenceLookupService;
import be.angularpadelclub.Service.ReservationCancellationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CourtServiceTest {

    private CourtRepository courtRepository;
    private ReservationRepository reservationRepository;
    private ReservationCancellationService reservationCancellationService;
    private ReferenceLookupService referenceLookupService;
    private CourtMapper courtMapper;
    private CourtService service;

    @BeforeEach
    void setUp() {
        courtRepository = mock(CourtRepository.class);
        reservationRepository = mock(ReservationRepository.class);
        reservationCancellationService = mock(ReservationCancellationService.class);
        referenceLookupService = mock(ReferenceLookupService.class);
        courtMapper = mock(CourtMapper.class);

        service = new CourtService(
                courtRepository,
                reservationRepository,
                reservationCancellationService,
                referenceLookupService,
                courtMapper
        );
    }

    @Test
    void getAllCourts_returnsOnlyActiveMappedCourts() {
        CourtEntity court = new CourtEntity();
        CourtDTO dto = dto(false);

        when(courtRepository.findByActifTrue()).thenReturn(List.of(court));
        when(courtMapper.toDTO(court)).thenReturn(dto);

        List<CourtDTO> result = service.getAllCourts();

        assertEquals(1, result.size());
        assertSame(dto, result.get(0));
    }

    @Test
    void getCourtById_usesReferenceLookupAndMapper() {
        CourtEntity court = new CourtEntity();
        CourtDTO dto = dto(false);

        when(referenceLookupService.findCourtOrThrow(1)).thenReturn(court);
        when(courtMapper.toDTO(court)).thenReturn(dto);

        CourtDTO result = service.getCourtById(1);

        assertSame(dto, result);
    }

    @Test
    void createCourt_rejectsInactiveSite() {
        SiteEntity site = new SiteEntity();
        site.setActif(false);

        when(referenceLookupService.findSiteOrThrow(1)).thenReturn(site);

        assertThrows(
                ResponseStatusException.class,
                () -> service.createCourt(dto(false))
        );
    }

    @Test
    void createCourt_savesActiveCourtWithoutMaintenance() {
        SiteEntity site = new SiteEntity();
        site.setActif(true);

        CourtDTO input = dto(false);
        CourtEntity entity = new CourtEntity();
        CourtEntity saved = new CourtEntity();
        CourtDTO output = dto(false);

        when(referenceLookupService.findSiteOrThrow(1)).thenReturn(site);
        when(courtMapper.toEntity(input, site)).thenReturn(entity);
        when(courtRepository.save(entity)).thenReturn(saved);
        when(courtMapper.toDTO(saved)).thenReturn(output);

        CourtDTO result = service.createCourt(input);

        assertSame(output, result);
        assertEquals(true, entity.isActif());
        assertEquals(false, entity.isMaintenance());
        verify(courtRepository).save(entity);
    }

    @Test
    void setMaintenance_rejectsInactiveCourt() {
        CourtEntity court = new CourtEntity();
        court.setActif(false);

        when(referenceLookupService.findCourtOrThrow(1)).thenReturn(court);

        assertThrows(
                ResponseStatusException.class,
                () -> service.setMaintenance(1, true)
        );
    }

    @Test
    void setMaintenance_returnsCurrentDtoWhenStateDoesNotChange() {
        CourtEntity court = new CourtEntity();
        court.setActif(true);
        court.setMaintenance(true);

        CourtDTO dto = dto(true);

        when(referenceLookupService.findCourtOrThrow(1)).thenReturn(court);
        when(courtMapper.toDTO(court)).thenReturn(dto);

        CourtDTO result = service.setMaintenance(1, true);

        assertSame(dto, result);
    }

    @Test
    void setMaintenanceTrue_cancelsFutureReservations() {
        CourtEntity court = new CourtEntity();
        court.setActif(true);
        court.setMaintenance(false);

        CourtEntity saved = new CourtEntity();
        saved.setMaintenance(true);

        ReservationEntity reservation = new ReservationEntity();
        CourtDTO output = dto(true);

        when(referenceLookupService.findCourtOrThrow(1)).thenReturn(court);
        when(courtRepository.save(court)).thenReturn(saved);
        when(reservationRepository.findByCourtIdAndDateGreaterThanEqual(any(Integer.class), any(LocalDate.class)))
                .thenReturn(List.of(reservation));
        when(courtMapper.toDTO(saved)).thenReturn(output);

        CourtDTO result = service.setMaintenance(1, true);

        assertSame(output, result);
        assertEquals(true, court.isMaintenance());
        verify(reservationCancellationService)
                .cancelReservationsForClubReason(List.of(reservation));
    }

    @Test
    void deleteCourt_deletesCourtWithoutFutureReservations() {
        CourtEntity court = new CourtEntity();

        when(referenceLookupService.findCourtOrThrow(1)).thenReturn(court);
        when(reservationRepository.existsByCourtIdAndDateAfter(any(Integer.class), any(LocalDate.class)))
                .thenReturn(false);

        service.deleteCourt(1);

        verify(courtRepository).delete(court);
    }

    @Test
    void deleteCourt_disablesCourtWhenFutureReservationsExist() {
        CourtEntity court = new CourtEntity();
        court.setActif(true);

        when(referenceLookupService.findCourtOrThrow(1)).thenReturn(court);
        when(reservationRepository.existsByCourtIdAndDateAfter(any(Integer.class), any(LocalDate.class)))
                .thenReturn(true);

        assertThrows(
                ResponseStatusException.class,
                () -> service.deleteCourt(1)
        );

        assertEquals(false, court.isActif());
        verify(courtRepository).save(court);
    }

    private CourtDTO dto(boolean maintenance) {
        return new CourtDTO(
                1,
                "Terrain 1",
                1,
                true,
                true,
                maintenance
        );
    }
}