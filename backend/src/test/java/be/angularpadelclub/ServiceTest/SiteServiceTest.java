package be.angularpadelclub.ServiceTest;

import be.angularpadelclub.DTO.SiteDTO;
import be.angularpadelclub.Entity.HoraireSiteEntity;
import be.angularpadelclub.Entity.SiteEntity;
import be.angularpadelclub.Mapper.SiteMapper;
import be.angularpadelclub.Repository.HoraireSiteRepository;
import be.angularpadelclub.Repository.SiteRepository;
import be.angularpadelclub.Service.ReferenceLookupService;
import be.angularpadelclub.Service.SiteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SiteServiceTest {

    private SiteRepository siteRepository;
    private HoraireSiteRepository horaireSiteRepository;
    private SiteMapper siteMapper;
    private ReferenceLookupService referenceLookupService;
    private SiteService service;

    @BeforeEach
    void setUp() {
        siteRepository = mock(SiteRepository.class);
        horaireSiteRepository = mock(HoraireSiteRepository.class);
        siteMapper = mock(SiteMapper.class);
        referenceLookupService = mock(ReferenceLookupService.class);

        service = new SiteService(
                siteRepository,
                horaireSiteRepository,
                siteMapper,
                referenceLookupService
        );
    }

    @Test
    void getAllSites_mapsSitesWithCurrentYearSchedule() {
        SiteEntity site = new SiteEntity();
        site.setId(1);

        HoraireSiteEntity horaire = new HoraireSiteEntity();
        SiteDTO dto = validDto();

        when(siteRepository.findAll()).thenReturn(List.of(site));
        when(horaireSiteRepository.findBySite_IdAndAnnee(1, LocalDate.now().getYear()))
                .thenReturn(Optional.of(horaire));
        when(siteMapper.toDTO(site, horaire)).thenReturn(dto);

        List<SiteDTO> result = service.getAllSites();

        assertEquals(1, result.size());
        assertSame(dto, result.get(0));
    }

    @Test
    void getSiteById_usesReferenceLookupAndCurrentYearSchedule() {
        SiteEntity site = new SiteEntity();
        site.setId(1);

        HoraireSiteEntity horaire = new HoraireSiteEntity();
        SiteDTO dto = validDto();

        when(referenceLookupService.findSiteOrThrow(1)).thenReturn(site);
        when(horaireSiteRepository.findBySite_IdAndAnnee(1, LocalDate.now().getYear()))
                .thenReturn(Optional.of(horaire));
        when(siteMapper.toDTO(site, horaire)).thenReturn(dto);

        SiteDTO result = service.getSiteById(1);

        assertSame(dto, result);
    }

    @Test
    void createSite_rejectsNullDto() {
        assertThrows(
                ResponseStatusException.class,
                () -> service.createSite(null)
        );
    }

    @Test
    void createSite_rejectsMissingName() {
        SiteDTO dto = new SiteDTO(
                null,
                " ",
                "Bruxelles",
                "Rue du Test 1",
                "1000",
                "Site test",
                LocalTime.of(8, 0),
                LocalTime.of(22, 0),
                true,
                "/assets/site.jpg",
                List.of()
        );

        assertThrows(
                ResponseStatusException.class,
                () -> service.createSite(dto)
        );
    }

    @Test
    void createSite_rejectsOpeningAfterClosing() {
        SiteDTO dto = new SiteDTO(
                null,
                "Brussels Padel",
                "Bruxelles",
                "Rue du Test 1",
                "1000",
                "Site test",
                LocalTime.of(22, 0),
                LocalTime.of(8, 0),
                true,
                "/assets/site.jpg",
                List.of()
        );

        assertThrows(
                ResponseStatusException.class,
                () -> service.createSite(dto)
        );
    }

    @Test
    void createSite_savesSiteAndDefaultSchedule() {
        SiteDTO input = validDto();

        SiteEntity entity = new SiteEntity();
        SiteEntity savedSite = new SiteEntity();
        savedSite.setId(1);

        HoraireSiteEntity savedHoraire = new HoraireSiteEntity();
        SiteDTO output = validDto();

        when(siteMapper.toEntity(input)).thenReturn(entity);
        when(siteRepository.save(entity)).thenReturn(savedSite);
        when(horaireSiteRepository.save(any(HoraireSiteEntity.class)))
                .thenReturn(savedHoraire);
        when(siteMapper.toDTO(savedSite, savedHoraire)).thenReturn(output);

        SiteDTO result = service.createSite(input);

        assertSame(output, result);
        verify(siteRepository).save(entity);
        verify(horaireSiteRepository).save(any(HoraireSiteEntity.class));
    }

    @Test
    void updateSite_rejectsNullDto() {
        assertThrows(
                ResponseStatusException.class,
                () -> service.updateSite(1, null)
        );
    }

    @Test
    void updateSite_updatesEditableFieldsAndSchedule() {
        SiteEntity site = new SiteEntity();
        site.setId(1);
        site.setNom("Old");
        site.setAdresse("Old address");
        site.setVille("Old city");
        site.setCode_postal("0000");
        site.setDescription("Old description");
        site.setImage_url("old.jpg");
        site.setActif(true);

        HoraireSiteEntity horaire = new HoraireSiteEntity();
        horaire.setSite(site);
        horaire.setAnnee(LocalDate.now().getYear());
        horaire.setHeure_debut(LocalTime.of(8, 0));
        horaire.setHeure_fin(LocalTime.of(22, 0));

        SiteDTO input = validDto();
        SiteDTO output = validDto();

        when(referenceLookupService.findSiteOrThrow(1)).thenReturn(site);
        when(siteRepository.save(site)).thenReturn(site);
        when(horaireSiteRepository.findBySite_IdAndAnnee(1, LocalDate.now().getYear()))
                .thenReturn(Optional.of(horaire));
        when(horaireSiteRepository.save(horaire)).thenReturn(horaire);
        when(siteMapper.toDTO(site, horaire)).thenReturn(output);

        SiteDTO result = service.updateSite(1, input);

        assertSame(output, result);
        assertEquals("Brussels Padel", site.getNom());
        assertEquals("Bruxelles", site.getVille());
        assertEquals("Rue du Test 1", site.getAdresse());
        verify(siteRepository).save(site);
        verify(horaireSiteRepository).save(horaire);
    }

    @Test
    void updateSite_rejectsInvalidScheduleHours() {
        SiteEntity site = new SiteEntity();
        site.setId(1);

        HoraireSiteEntity horaire = new HoraireSiteEntity();
        horaire.setSite(site);
        horaire.setAnnee(LocalDate.now().getYear());
        horaire.setHeure_debut(LocalTime.of(8, 0));
        horaire.setHeure_fin(LocalTime.of(22, 0));

        SiteDTO input = new SiteDTO(
                null,
                "Brussels Padel",
                "Bruxelles",
                "Rue du Test 1",
                "1000",
                "Site test",
                LocalTime.of(23, 0),
                LocalTime.of(8, 0),
                true,
                "/assets/site.jpg",
                List.of()
        );

        when(referenceLookupService.findSiteOrThrow(1)).thenReturn(site);
        when(siteRepository.save(site)).thenReturn(site);
        when(horaireSiteRepository.findBySite_IdAndAnnee(1, LocalDate.now().getYear()))
                .thenReturn(Optional.of(horaire));

        assertThrows(
                ResponseStatusException.class,
                () -> service.updateSite(1, input)
        );
    }

    @Test
    void deleteSite_disablesActiveSite() {
        SiteEntity site = new SiteEntity();
        site.setActif(true);

        when(referenceLookupService.findSiteOrThrow(1)).thenReturn(site);

        service.deleteSite(1);

        assertEquals(false, site.isActif());
        verify(siteRepository).save(site);
    }

    @Test
    void deleteSite_rejectsAlreadyInactiveSite() {
        SiteEntity site = new SiteEntity();
        site.setActif(false);

        when(referenceLookupService.findSiteOrThrow(1)).thenReturn(site);

        assertThrows(
                ResponseStatusException.class,
                () -> service.deleteSite(1)
        );
    }

    private SiteDTO validDto() {
        return new SiteDTO(
                1,
                "Brussels Padel",
                "Bruxelles",
                "Rue du Test 1",
                "1000",
                "Site test",
                LocalTime.of(8, 0),
                LocalTime.of(22, 0),
                true,
                "/assets/site.jpg",
                List.of()
        );
    }
}