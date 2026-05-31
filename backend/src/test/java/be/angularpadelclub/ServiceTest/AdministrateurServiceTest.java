package be.angularpadelclub.ServiceTest;

import be.angularpadelclub.Entity.AdministrateurEntity;
import be.angularpadelclub.Repository.AdministrateurRepository;
import be.angularpadelclub.Service.AdministrateurService;
import be.angularpadelclub.Service.ReferenceLookupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdministrateurServiceTest {

    private AdministrateurRepository administrateurRepository;
    private ReferenceLookupService referenceLookupService;
    private AdministrateurService service;

    @BeforeEach
    void setUp() {
        administrateurRepository = mock(AdministrateurRepository.class);
        referenceLookupService = mock(ReferenceLookupService.class);

        service = new AdministrateurService(
                administrateurRepository,
                referenceLookupService
        );
    }

    @Test
    void findAll_delegatesToRepository() {
        AdministrateurEntity admin = new AdministrateurEntity();

        when(administrateurRepository.findAll()).thenReturn(List.of(admin));

        List<AdministrateurEntity> result = service.findAll();

        assertEquals(1, result.size());
        assertSame(admin, result.getFirst());
        verify(administrateurRepository).findAll();
    }

    @Test
    void findById_delegatesToReferenceLookupService() {
        AdministrateurEntity admin = new AdministrateurEntity();

        when(referenceLookupService.findAdministrateurOrThrow(1)).thenReturn(admin);

        AdministrateurEntity result = service.findById(1);

        assertSame(admin, result);
        verify(referenceLookupService).findAdministrateurOrThrow(1);
    }

    @Test
    void findByMatricule_delegatesToReferenceLookupService() {
        AdministrateurEntity admin = new AdministrateurEntity();

        when(referenceLookupService.findAdministrateurByMatriculeOrThrow("A0001"))
                .thenReturn(admin);

        AdministrateurEntity result = service.findByMatricule("A0001");

        assertSame(admin, result);
        verify(referenceLookupService).findAdministrateurByMatriculeOrThrow("A0001");
    }

    @Test
    void findByTypeAdmin_delegatesToRepository() {
        AdministrateurEntity admin = new AdministrateurEntity();

        when(administrateurRepository.findByTypeAdmin("GLOBAL"))
                .thenReturn(List.of(admin));

        List<AdministrateurEntity> result = service.findByTypeAdmin("GLOBAL");

        assertEquals(1, result.size());
        assertSame(admin, result.getFirst());
        verify(administrateurRepository).findByTypeAdmin("GLOBAL");
    }

    @Test
    void findBySiteId_delegatesToRepository() {
        AdministrateurEntity admin = new AdministrateurEntity();

        when(administrateurRepository.findBySiteId(1))
                .thenReturn(List.of(admin));

        List<AdministrateurEntity> result = service.findBySiteId(1);

        assertEquals(1, result.size());
        assertSame(admin, result.getFirst());
        verify(administrateurRepository).findBySiteId(1);
    }
}