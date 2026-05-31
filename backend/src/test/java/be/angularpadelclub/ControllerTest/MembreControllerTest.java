package be.angularpadelclub.ControllerTest;

import be.angularpadelclub.Controler.MembreController;
import be.angularpadelclub.DTO.MembreDTO;
import be.angularpadelclub.Entity.MembreEntity;
import be.angularpadelclub.Entity.TypeMembreEntity;
import be.angularpadelclub.Exception.GlobalExceptionHandler;
import be.angularpadelclub.Mapper.MembreMapper;
import be.angularpadelclub.Service.MembreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MembreControllerTest {

    private MembreService membreService;
    private MembreMapper membreMapper;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        membreService = mock(MembreService.class);
        membreMapper = mock(MembreMapper.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new MembreController(membreService, membreMapper))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void findByMatricule_returnsMember() throws Exception {
        MembreEntity member = new MembreEntity();

        when(membreService.findByMatricule("G0001")).thenReturn(Optional.of(member));
        when(membreMapper.toDTO(member)).thenReturn(dto(1, "G0001"));

        mockMvc.perform(get("/api/members/G0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matricule").value("G0001"));
    }

    @Test
    void findByMatricule_returns404WhenMissing() throws Exception {
        when(membreService.findByMatricule("X9999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/members/X9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Membre introuvable")));
    }

    @Test
    void findVisibleByAdmin_delegatesToService() throws Exception {
        MembreEntity member = new MembreEntity();

        when(membreService.findVisibleByAdmin("A0001")).thenReturn(List.of(member));
        when(membreMapper.toDTOList(List.of(member))).thenReturn(List.of(dto(2, "S0002")));

        mockMvc.perform(get("/api/members/admin/A0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].matricule").value("S0002"));
    }

    @Test
    void addMemberAsAdmin_returnsCreated() throws Exception {
        mockMvc.perform(post("/api/members/admin/A0001")
                        .contentType("application/json")
                        .content("""
                                {
                                  "active": true,
                                  "email": "test@test.be",
                                  "matricule": "G9999",
                                  "firstName": "Jean",
                                  "lastName": "Dupont",
                                  "type": {
                                    "id": 1,
                                    "code": "GLOBAL",
                                    "delai_reservation_jours": 21
                                  }
                                }
                                """))
                .andExpect(status().isCreated());

        verify(membreService).addMemberAsAdmin(eq("A0001"), any(MembreDTO.class));
    }

    @Test
    void updateActiveStatus_validatesRequiredBoolean() throws Exception {
        mockMvc.perform(patch("/api/members/1/active")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("actif")));
    }

    @Test
    void updateOwnProfile_delegatesAndReturnsDto() throws Exception {
        MembreEntity member = new MembreEntity();

        when(membreService.updateOwnProfile(1, "Jean", "Durand", "jean@test.be"))
                .thenReturn(member);
        when(membreMapper.toDTO(member))
                .thenReturn(dto(1, "G0001"));

        mockMvc.perform(patch("/api/members/1/profile")
                        .contentType("application/json")
                        .content("""
                                {
                                  "firstName": "Jean",
                                  "lastName": "Durand",
                                  "email": "jean@test.be"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteMember_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/members/1"))
                .andExpect(status().isNoContent());

        verify(membreService).deleteMember(1);
    }

    private MembreDTO dto(Integer id, String matricule) {
        return new MembreDTO(
                id,
                true,
                "test@test.be",
                matricule,
                "Jean",
                "Dupont",
                typeGlobal(),
                1,
                "Brussels Padel"
        );
    }

    private TypeMembreEntity typeGlobal() {
        TypeMembreEntity type = new TypeMembreEntity();
        type.setId(1);
        type.setCode("GLOBAL");
        type.setDelai_reservation_jours(21);
        return type;
    }
}