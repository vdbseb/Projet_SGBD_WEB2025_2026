package be.angularpadelclub.ServiceTest;

import be.angularpadelclub.Entity.AdministrateurEntity;
import be.angularpadelclub.Entity.CourtEntity;
import be.angularpadelclub.Entity.MembreEntity;
import be.angularpadelclub.Entity.SiteEntity;
import be.angularpadelclub.Repository.AdministrateurRepository;
import be.angularpadelclub.Repository.CourtRepository;
import be.angularpadelclub.Repository.HoraireSiteRepository;
import be.angularpadelclub.Repository.JourFermetureRepository;
import be.angularpadelclub.Repository.MatchRepository;
import be.angularpadelclub.Repository.MembreRepository;
import be.angularpadelclub.Repository.PaiementRepository;
import be.angularpadelclub.Repository.ParticipationRepository;
import be.angularpadelclub.Repository.PenaliteRepository;
import be.angularpadelclub.Repository.ReservationRepository;
import be.angularpadelclub.Repository.SiteRepository;
import be.angularpadelclub.Service.ReferenceLookupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReferenceLookupServiceTest {

    private AdministrateurRepository administrateurRepository;
    private CourtRepository courtRepository;
    private MembreRepository membreRepository;
    private SiteRepository siteRepository;

    private ReferenceLookupService service;

    @BeforeEach
    void setUp() {
        administrateurRepository = mock(AdministrateurRepository.class);
        courtRepository = mock(CourtRepository.class);
        membreRepository = mock(MembreRepository.class);
        siteRepository = mock(SiteRepository.class);

        service = new ReferenceLookupService(
                administrateurRepository,
                courtRepository,
                mock(HoraireSiteRepository.class),
                mock(JourFermetureRepository.class),
                mock(MatchRepository.class),
                membreRepository,
                mock(PaiementRepository.class),
                mock(ParticipationRepository.class),
                mock(PenaliteRepository.class),
                mock(ReservationRepository.class),
                siteRepository
        );
    }

    @Test
    void findAdministrateurOrThrow_returnsEntityWhenFound() {
        AdministrateurEntity admin = new AdministrateurEntity();

        when(administrateurRepository.findById(1)).thenReturn(Optional.of(admin));

        AdministrateurEntity result = service.findAdministrateurOrThrow(1);

        assertSame(admin, result);
    }

    @Test
    void findAdministrateurOrThrow_rejectsNullId() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.findAdministrateurOrThrow(null)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void findAdministrateurOrThrow_returns404WhenMissing() {
        when(administrateurRepository.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.findAdministrateurOrThrow(99)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void findAdministrateurByMatriculeOrThrow_returnsEntityWhenFound() {
        AdministrateurEntity admin = new AdministrateurEntity();

        when(administrateurRepository.findByMatricule("A0001"))
                .thenReturn(Optional.of(admin));

        AdministrateurEntity result =
                service.findAdministrateurByMatriculeOrThrow("A0001");

        assertSame(admin, result);
    }

    @Test
    void findAdministrateurByMatriculeOrThrow_rejectsBlankMatricule() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.findAdministrateurByMatriculeOrThrow(" ")
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void findCourtOrThrow_returnsEntityWhenFound() {
        CourtEntity court = new CourtEntity();

        when(courtRepository.findById(1)).thenReturn(Optional.of(court));

        CourtEntity result = service.findCourtOrThrow(1);

        assertSame(court, result);
    }

    @Test
    void findMembreOrThrow_returnsEntityWhenFound() {
        MembreEntity membre = new MembreEntity();

        when(membreRepository.findById(1)).thenReturn(Optional.of(membre));

        MembreEntity result = service.findMembreOrThrow(1);

        assertSame(membre, result);
    }

    @Test
    void findMembreByMatriculeOrThrow_returnsEntityWhenFound() {
        MembreEntity membre = new MembreEntity();

        when(membreRepository.findByMatricule("G0001"))
                .thenReturn(Optional.of(membre));

        MembreEntity result = service.findMembreByMatriculeOrThrow("G0001");

        assertSame(membre, result);
    }

    @Test
    void findSiteOrThrow_returnsEntityWhenFound() {
        SiteEntity site = new SiteEntity();

        when(siteRepository.findById(1)).thenReturn(Optional.of(site));

        SiteEntity result = service.findSiteOrThrow(1);

        assertSame(site, result);
    }

    @Test
    void findSiteOrThrow_returns404WhenMissing() {
        when(siteRepository.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.findSiteOrThrow(99)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}