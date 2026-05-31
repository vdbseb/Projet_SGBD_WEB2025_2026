package be.angularpadelclub.ServiceTest;

import be.angularpadelclub.DTO.HoraireSiteDTO;
import be.angularpadelclub.Entity.HoraireSiteEntity;
import be.angularpadelclub.Entity.SiteEntity;
import be.angularpadelclub.Repository.HoraireSiteRepository;
import be.angularpadelclub.Service.ClubBusinessRules;
import be.angularpadelclub.Service.HoraireSiteService;
import be.angularpadelclub.Service.ReferenceLookupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HoraireSiteServiceTest {

    private HoraireSiteRepository horaireSiteRepository;
    private ReferenceLookupService referenceLookupService;
    private HoraireSiteService service;

    @BeforeEach
    void setUp() {
        horaireSiteRepository = mock(HoraireSiteRepository.class);
        referenceLookupService = mock(ReferenceLookupService.class);

        service = new HoraireSiteService(
                horaireSiteRepository,
                referenceLookupService
        );
    }

    @Test
    void findAll_delegatesToRepository() {
        HoraireSiteEntity horaire = new HoraireSiteEntity();

        when(horaireSiteRepository.findAll()).thenReturn(List.of(horaire));

        List<HoraireSiteEntity> result = service.findAll();

        assertEquals(1, result.size());
        assertSame(horaire, result.get(0));
    }

    @Test
    void findById_usesReferenceLookup() {
        HoraireSiteEntity horaire = new HoraireSiteEntity();

        when(referenceLookupService.findHoraireSiteOrThrow(1)).thenReturn(horaire);

        HoraireSiteEntity result = service.findById(1);

        assertSame(horaire, result);
    }

    @Test
    void findBySiteId_checksSiteThenReturnsSchedules() {
        SiteEntity site = new SiteEntity();
        HoraireSiteEntity horaire = new HoraireSiteEntity();

        when(referenceLookupService.findSiteOrThrow(1)).thenReturn(site);
        when(horaireSiteRepository.findBySite_Id(1)).thenReturn(List.of(horaire));

        List<HoraireSiteEntity> result = service.findBySiteId(1);

        assertEquals(1, result.size());
        assertSame(horaire, result.get(0));
        verify(referenceLookupService).findSiteOrThrow(1);
    }

    @Test
    void findBySiteIdAndAnnee_returnsScheduleWhenFound() {
        SiteEntity site = new SiteEntity();
        HoraireSiteEntity horaire = new HoraireSiteEntity();

        when(referenceLookupService.findSiteOrThrow(1)).thenReturn(site);
        when(horaireSiteRepository.findBySite_IdAndAnnee(1, 2026))
                .thenReturn(Optional.of(horaire));

        HoraireSiteEntity result = service.findBySiteIdAndAnnee(1, 2026);

        assertSame(horaire, result);
    }

    @Test
    void findBySiteIdAndAnnee_returns404WhenMissing() {
        SiteEntity site = new SiteEntity();

        when(referenceLookupService.findSiteOrThrow(1)).thenReturn(site);
        when(horaireSiteRepository.findBySite_IdAndAnnee(1, 2026))
                .thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> service.findBySiteIdAndAnnee(1, 2026)
        );
    }

    @Test
    void create_rejectsNullDto() {
        assertThrows(
                ResponseStatusException.class,
                () -> service.create(null)
        );
    }

    @Test
    void create_rejectsInvalidYear() {
        assertThrows(
                ResponseStatusException.class,
                () -> service.create(dto(1, 2019, LocalTime.of(8, 0), LocalTime.of(22, 0), 90, 15))
        );
    }

    @Test
    void create_rejectsEndBeforeStart() {
        assertThrows(
                ResponseStatusException.class,
                () -> service.create(dto(1, 2026, LocalTime.of(22, 0), LocalTime.of(8, 0), 90, 15))
        );
    }

    @Test
    void create_rejectsWrongMatchDuration() {
        assertThrows(
                ResponseStatusException.class,
                () -> service.create(dto(1, 2026, LocalTime.of(8, 0), LocalTime.of(22, 0), 60, 15))
        );
    }

    @Test
    void create_rejectsWrongPauseDuration() {
        assertThrows(
                ResponseStatusException.class,
                () -> service.create(dto(1, 2026, LocalTime.of(8, 0), LocalTime.of(22, 0), 90, 10))
        );
    }

    @Test
    void create_rejectsDuplicateSchedule() {
        SiteEntity site = new SiteEntity();

        when(referenceLookupService.findSiteOrThrow(1)).thenReturn(site);
        when(horaireSiteRepository.existsBySite_IdAndAnnee(1, 2026)).thenReturn(true);

        assertThrows(
                ResponseStatusException.class,
                () -> service.create(validDto())
        );
    }

    @Test
    void create_savesValidSchedule() {
        SiteEntity site = new SiteEntity();
        HoraireSiteEntity saved = new HoraireSiteEntity();

        when(referenceLookupService.findSiteOrThrow(1)).thenReturn(site);
        when(horaireSiteRepository.existsBySite_IdAndAnnee(1, 2026)).thenReturn(false);
        when(horaireSiteRepository.save(org.mockito.ArgumentMatchers.any(HoraireSiteEntity.class)))
                .thenReturn(saved);

        HoraireSiteEntity result = service.create(validDto());

        assertSame(saved, result);
    }

    @Test
    void update_rejectsDuplicateOtherSchedule() {
        HoraireSiteEntity current = new HoraireSiteEntity();
        current.setId(1);

        HoraireSiteEntity other = new HoraireSiteEntity();
        other.setId(2);

        SiteEntity site = new SiteEntity();

        when(referenceLookupService.findHoraireSiteOrThrow(1)).thenReturn(current);
        when(referenceLookupService.findSiteOrThrow(1)).thenReturn(site);
        when(horaireSiteRepository.findBySite_IdAndAnnee(1, 2026))
                .thenReturn(Optional.of(other));

        assertThrows(
                ResponseStatusException.class,
                () -> service.update(1, validDto())
        );
    }

    @Test
    void delete_usesReferenceLookupThenRepositoryDelete() {
        HoraireSiteEntity horaire = new HoraireSiteEntity();

        when(referenceLookupService.findHoraireSiteOrThrow(1)).thenReturn(horaire);

        service.delete(1);

        verify(horaireSiteRepository).delete(horaire);
    }

    private HoraireSiteDTO validDto() {
        return dto(
                1,
                2026,
                LocalTime.of(8, 0),
                LocalTime.of(22, 0),
                ClubBusinessRules.DEFAULT_MATCH_DURATION_MINUTES,
                ClubBusinessRules.DEFAULT_PAUSE_MINUTES
        );
    }

    private HoraireSiteDTO dto(
            Integer siteId,
            int annee,
            LocalTime start,
            LocalTime end,
            int matchMinutes,
            int pauseMinutes
    ) {
        return new HoraireSiteDTO(
                null,
                siteId,
                "Brussels Padel",
                annee,
                start,
                end,
                matchMinutes,
                pauseMinutes
        );
    }
}