package be.angularpadelclub.ServiceTest;

import be.angularpadelclub.DTO.MembreDTO;
import be.angularpadelclub.Entity.AdministrateurEntity;
import be.angularpadelclub.Entity.MembreEntity;
import be.angularpadelclub.Entity.SiteEntity;
import be.angularpadelclub.Entity.TypeMembreEntity;
import be.angularpadelclub.Mapper.MembreMapper;
import be.angularpadelclub.Repository.MembreRepository;
import be.angularpadelclub.Service.MembreService;
import be.angularpadelclub.Service.ReferenceLookupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MembreServiceTest {

    private MembreRepository membreRepository;
    private MembreMapper membreMapper;
    private ReferenceLookupService referenceLookupService;
    private MembreService service;

    @BeforeEach
    void setUp() {
        membreRepository = mock(MembreRepository.class);
        membreMapper = mock(MembreMapper.class);
        referenceLookupService = mock(ReferenceLookupService.class);

        service = new MembreService(
                membreRepository,
                membreMapper,
                referenceLookupService
        );
    }

    @Test
    void findAll_delegatesToRepository() {
        MembreEntity membre = new MembreEntity();

        when(membreRepository.findAll()).thenReturn(List.of(membre));

        List<MembreEntity> result = service.findAll();

        assertEquals(1, result.size());
        assertSame(membre, result.get(0));
        verify(membreRepository).findAll();
    }

    @Test
    void findVisibleByAdmin_returnsAllMembersForGlobalAdmin() {
        AdministrateurEntity admin = new AdministrateurEntity();
        admin.setTypeAdmin("GLOBAL");

        MembreEntity membre = new MembreEntity();

        when(referenceLookupService.findAdministrateurByMatriculeOrThrow("A0001"))
                .thenReturn(admin);
        when(membreRepository.findAll()).thenReturn(List.of(membre));

        List<MembreEntity> result = service.findVisibleByAdmin("A0001");

        assertEquals(1, result.size());
        assertSame(membre, result.get(0));
        verify(membreRepository).findAll();
    }

    @Test
    void findVisibleByAdmin_returnsSiteMembersForSiteAdmin() {
        SiteEntity site = new SiteEntity();
        site.setId(3);

        AdministrateurEntity admin = new AdministrateurEntity();
        admin.setTypeAdmin("SITE");
        admin.setSite(site);

        MembreEntity membre = new MembreEntity();

        when(referenceLookupService.findAdministrateurByMatriculeOrThrow("A0002"))
                .thenReturn(admin);
        when(membreRepository.findBySiteId(3)).thenReturn(List.of(membre));

        List<MembreEntity> result = service.findVisibleByAdmin("A0002");

        assertEquals(1, result.size());
        assertSame(membre, result.get(0));
        verify(membreRepository).findBySiteId(3);
    }

    @Test
    void findVisibleByAdmin_rejectsUnknownAdminType() {
        AdministrateurEntity admin = new AdministrateurEntity();
        admin.setTypeAdmin("AUTRE");

        when(referenceLookupService.findAdministrateurByMatriculeOrThrow("A9999"))
                .thenReturn(admin);

        assertThrows(
                ResponseStatusException.class,
                () -> service.findVisibleByAdmin("A9999")
        );
    }

    @Test
    void findById_delegatesToRepository() {
        MembreEntity membre = new MembreEntity();

        when(membreRepository.findById(1)).thenReturn(Optional.of(membre));

        Optional<MembreEntity> result = service.findById(1);

        assertTrue(result.isPresent());
        assertSame(membre, result.get());
    }

    @Test
    void findByMatricule_delegatesToRepository() {
        MembreEntity membre = new MembreEntity();

        when(membreRepository.findByMatricule("G0001"))
                .thenReturn(Optional.of(membre));

        Optional<MembreEntity> result = service.findByMatricule("G0001");

        assertTrue(result.isPresent());
        assertSame(membre, result.get());
    }

    @Test
    void findByNomAndPrenom_rejectsBlankNom() {
        assertThrows(
                ResponseStatusException.class,
                () -> service.findByNomAndPrenom(" ", "Jean")
        );
    }

    @Test
    void findByNomAndPrenom_rejectsBlankPrenom() {
        assertThrows(
                ResponseStatusException.class,
                () -> service.findByNomAndPrenom("Dupont", " ")
        );
    }

    @Test
    void getNextMatricule_generatesFirstGlobalMatricule() {
        when(membreRepository.findTopByMatriculeStartingWithOrderByMatriculeDesc("G"))
                .thenReturn(Optional.empty());

        String result = service.getNextMatricule("GLOBAL");

        assertEquals("G0001", result);
    }

    @Test
    void getNextMatricule_incrementsExistingMatricule() {
        MembreEntity last = new MembreEntity();
        last.setMatricule("S0007");

        when(membreRepository.findTopByMatriculeStartingWithOrderByMatriculeDesc("S"))
                .thenReturn(Optional.of(last));

        String result = service.getNextMatricule("SITE");

        assertEquals("S0008", result);
    }

    @Test
    void getNextMatricule_rejectsUnknownType() {
        assertThrows(
                ResponseStatusException.class,
                () -> service.getNextMatricule("VIP")
        );
    }

    @Test
    void updateMemberActiveStatus_updatesMember() {
        MembreEntity membre = new MembreEntity();
        membre.setActif(false);

        when(referenceLookupService.findMembreOrThrow(1)).thenReturn(membre);
        when(membreRepository.save(membre)).thenReturn(membre);

        MembreEntity result = service.updateMemberActiveStatus(1, true);

        assertSame(membre, result);
        assertTrue(result.isActif());
        verify(membreRepository).save(membre);
    }

    @Test
    void updateMemberActiveStatus_rejectsNullStatus() {
        assertThrows(
                ResponseStatusException.class,
                () -> service.updateMemberActiveStatus(1, null)
        );
    }

    @Test
    void updateOwnProfile_updatesValidProfile() {
        MembreEntity membre = new MembreEntity();
        membre.setActif(true);

        when(referenceLookupService.findMembreOrThrow(1)).thenReturn(membre);
        when(membreRepository.save(membre)).thenReturn(membre);

        MembreEntity result = service.updateOwnProfile(
                1,
                " Jean ",
                " Dupont ",
                " jean@test.be "
        );

        assertSame(membre, result);
        assertEquals("Jean", result.getPrenom());
        assertEquals("Dupont", result.getNom());
        assertEquals("jean@test.be", result.getEmail());
        verify(membreRepository).save(membre);
    }

    @Test
    void updateOwnProfile_rejectsInvalidEmail() {
        assertThrows(
                ResponseStatusException.class,
                () -> service.updateOwnProfile(1, "Jean", "Dupont", "bad-email")
        );
    }

    @Test
    void deleteMember_suspendsActiveMember() {
        MembreEntity membre = new MembreEntity();
        membre.setActif(true);

        when(referenceLookupService.findMembreOrThrow(1)).thenReturn(membre);

        service.deleteMember(1);

        assertEquals(false, membre.isActif());
        verify(membreRepository).save(membre);
    }

    @Test
    void addMember_rejectsDuplicateMatricule() {
        MembreDTO dto = memberDto("G0001", type("GLOBAL"), null);

        when(membreRepository.existsByMatricule("G0001")).thenReturn(true);

        assertThrows(
                ResponseStatusException.class,
                () -> service.addMember(dto)
        );
    }

    @Test
    void addMember_createsValidGlobalMember() {
        TypeMembreEntity type = type("GLOBAL");
        MembreDTO dto = memberDto("G0001", type, null);

        MembreEntity entity = new MembreEntity();

        when(membreRepository.existsByMatricule("G0001")).thenReturn(false);
        when(membreMapper.toEntity(dto, null)).thenReturn(entity);

        service.addMember(dto);

        verify(membreRepository).save(entity);
    }

    private MembreDTO memberDto(
            String matricule,
            TypeMembreEntity type,
            Integer siteId
    ) {
        return new MembreDTO(
                null,
                true,
                "jean@test.be",
                matricule,
                "Jean",
                "Dupont",
                type,
                siteId,
                siteId == null ? null : "Brussels Padel"
        );
    }

    private TypeMembreEntity type(String code) {
        TypeMembreEntity type = new TypeMembreEntity();
        type.setId(1);
        type.setCode(code);
        type.setDelai_reservation_jours(21);
        return type;
    }
}