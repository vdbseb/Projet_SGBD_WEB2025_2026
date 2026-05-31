package be.angularpadelclub.ControllerTest;

import be.angularpadelclub.Controler.AdministrateurController;
import be.angularpadelclub.Controler.CourtController;
import be.angularpadelclub.Controler.HoraireSiteController;
import be.angularpadelclub.Controler.JourFermetureController;
import be.angularpadelclub.Controler.PenaliteController;
import be.angularpadelclub.DTO.AdministrateurDTO;
import be.angularpadelclub.DTO.CourtDTO;
import be.angularpadelclub.DTO.HoraireSiteDTO;
import be.angularpadelclub.DTO.JourFermetureDTO;
import be.angularpadelclub.DTO.PenaliteDTO;
import be.angularpadelclub.DTO.SiteDTO;
import be.angularpadelclub.Entity.AdministrateurEntity;
import be.angularpadelclub.Entity.HoraireSiteEntity;
import be.angularpadelclub.Entity.JourFermetureEntity;
import be.angularpadelclub.Exception.GlobalExceptionHandler;
import be.angularpadelclub.Mapper.AdministrateurMapper;
import be.angularpadelclub.Mapper.HoraireSiteMapper;
import be.angularpadelclub.Mapper.JourFermetureMapper;
import be.angularpadelclub.Service.AdministrateurService;
import be.angularpadelclub.Service.CourtService;
import be.angularpadelclub.Service.HoraireSiteService;
import be.angularpadelclub.Service.JourFermetureService;
import be.angularpadelclub.Service.PenaliteService;
import be.angularpadelclub.Service.SiteService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminResourceControllerTest {

    private MockMvc mvc(Object controller) {
        return MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    class Sites {

        @Test
        void crudEndpoints_delegateToService() throws Exception {
            SiteService service = mock(SiteService.class);
            SiteDTO dto = siteDto(1);

            when(service.getAllSites()).thenReturn(List.of(dto));
            when(service.getSiteById(1)).thenReturn(dto);
            when(service.createSite(any())).thenReturn(dto);
            when(service.updateSite(eq(1), any())).thenReturn(dto);

            MockMvc mockMvc = mvc(new be.angularpadelclub.Controler.SiteController(service));

            mockMvc.perform(get("/api/sites"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1));

            mockMvc.perform(get("/api/sites/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Brussels Padel"));

            mockMvc.perform(post("/api/sites")
                            .contentType("application/json")
                            .content(siteJson()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1));

            mockMvc.perform(put("/api/sites/1")
                            .contentType("application/json")
                            .content(siteJson()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));

            mockMvc.perform(delete("/api/sites/1"))
                    .andExpect(status().isNoContent());

            verify(service).deleteSite(1);
        }
    }

    @Nested
    class Courts {

        @Test
        void crudAndMaintenanceEndpoints_delegateToService() throws Exception {
            CourtService service = mock(CourtService.class);
            CourtDTO dto = new CourtDTO(1, "Terrain 1", 1, true, true, false);

            when(service.getAllCourts()).thenReturn(List.of(dto));
            when(service.getCourtById(1)).thenReturn(dto);
            when(service.createCourt(any())).thenReturn(dto);
            when(service.updateCourt(eq(1), any())).thenReturn(dto);
            when(service.setMaintenance(1, true))
                    .thenReturn(new CourtDTO(1, "Terrain 1", 1, true, true, true));

            MockMvc mockMvc = mvc(new CourtController(service));

            mockMvc.perform(get("/api/courts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1));

            mockMvc.perform(get("/api/courts/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Terrain 1"));

            mockMvc.perform(post("/api/courts")
                            .contentType("application/json")
                            .content(courtJson()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1));

            mockMvc.perform(put("/api/courts/1")
                            .contentType("application/json")
                            .content(courtJson()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));

            mockMvc.perform(patch("/api/courts/1/maintenance")
                            .param("maintenance", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.maintenance").value(true));

            mockMvc.perform(delete("/api/courts/1"))
                    .andExpect(status().isNoContent());

            verify(service).deleteCourt(1);
        }
    }

    @Nested
    class SchedulesAndClosures {

        @Test
        void scheduleEndpoints_mapEntities() throws Exception {
            HoraireSiteService service = mock(HoraireSiteService.class);
            HoraireSiteMapper mapper = mock(HoraireSiteMapper.class);

            HoraireSiteEntity entity = new HoraireSiteEntity();
            HoraireSiteDTO dto = new HoraireSiteDTO(
                    1,
                    1,
                    "Brussels Padel",
                    2026,
                    LocalTime.of(8, 0),
                    LocalTime.of(22, 0),
                    90,
                    15
            );

            when(service.findAll()).thenReturn(List.of(entity));
            when(service.findById(1)).thenReturn(entity);
            when(service.findBySiteId(1)).thenReturn(List.of(entity));
            when(service.findBySiteIdAndAnnee(1, 2026)).thenReturn(entity);
            when(service.create(any())).thenReturn(entity);
            when(service.update(eq(1), any())).thenReturn(entity);
            when(mapper.toDTO(entity)).thenReturn(dto);
            when(mapper.toDTOList(List.of(entity))).thenReturn(List.of(dto));

            MockMvc mockMvc = mvc(new HoraireSiteController(service, mapper));

            mockMvc.perform(get("/api/horaires-sites"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].annee").value(2026));

            mockMvc.perform(get("/api/horaires-sites/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));

            mockMvc.perform(get("/api/horaires-sites/site/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].siteId").value(1));

            mockMvc.perform(get("/api/horaires-sites/site/1/annee/2026"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.annee").value(2026));

            mockMvc.perform(post("/api/horaires-sites")
                            .contentType("application/json")
                            .content(horaireJson()))
                    .andExpect(status().isCreated());

            mockMvc.perform(put("/api/horaires-sites/1")
                            .contentType("application/json")
                            .content(horaireJson()))
                    .andExpect(status().isOk());

            mockMvc.perform(delete("/api/horaires-sites/1"))
                    .andExpect(status().isNoContent());

            verify(service).delete(1);
        }

        @Test
        void closureEndpoints_mapEntities() throws Exception {
            JourFermetureService service = mock(JourFermetureService.class);
            JourFermetureMapper mapper = mock(JourFermetureMapper.class);

            JourFermetureEntity entity = new JourFermetureEntity();
            JourFermetureDTO dto = new JourFermetureDTO(
                    1,
                    1,
                    "Brussels Padel",
                    LocalDate.of(2026, 7, 21),
                    "Maintenance",
                    false
            );

            when(service.findAll()).thenReturn(List.of(entity));
            when(service.findById(1)).thenReturn(entity);
            when(service.findBySiteId(1)).thenReturn(List.of(entity));
            when(service.findGlobalClosures()).thenReturn(List.of(entity));
            when(service.create(any())).thenReturn(entity);
            when(service.update(eq(1), any())).thenReturn(entity);
            when(mapper.toDTO(entity)).thenReturn(dto);
            when(mapper.toDTOList(List.of(entity))).thenReturn(List.of(dto));

            MockMvc mockMvc = mvc(new JourFermetureController(service, mapper));

            mockMvc.perform(get("/api/jours-fermeture"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1));

            mockMvc.perform(get("/api/jours-fermeture/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.raison").value("Maintenance"));

            mockMvc.perform(get("/api/jours-fermeture/site/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].siteId").value(1));

            mockMvc.perform(get("/api/jours-fermeture/globales"))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/jours-fermeture")
                            .contentType("application/json")
                            .content(closureJson()))
                    .andExpect(status().isCreated());

            mockMvc.perform(put("/api/jours-fermeture/1")
                            .contentType("application/json")
                            .content(closureJson()))
                    .andExpect(status().isOk());

            mockMvc.perform(delete("/api/jours-fermeture/1"))
                    .andExpect(status().isNoContent());

            verify(service).delete(1);
        }
    }

    @Nested
    class AdminAndPenalties {

        @Test
        void administratorReadEndpoints_mapEntities() throws Exception {
            AdministrateurService service = mock(AdministrateurService.class);
            AdministrateurMapper mapper = mock(AdministrateurMapper.class);

            AdministrateurEntity entity = new AdministrateurEntity();
            AdministrateurDTO dto = new AdministrateurDTO(
                    1,
                    "A0001",
                    "Admin",
                    "Global",
                    "admin@test.be",
                    "GLOBAL",
                    null,
                    null
            );

            when(service.findAll()).thenReturn(List.of(entity));
            when(service.findById(1)).thenReturn(entity);
            when(service.findByMatricule("A0001")).thenReturn(entity);
            when(service.findByTypeAdmin("GLOBAL")).thenReturn(List.of(entity));
            when(service.findBySiteId(1)).thenReturn(List.of(entity));
            when(mapper.toDTO(entity)).thenReturn(dto);
            when(mapper.toDTOList(List.of(entity))).thenReturn(List.of(dto));

            MockMvc mockMvc = mvc(new AdministrateurController(service, mapper));

            mockMvc.perform(get("/api/administrateurs"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].matricule").value("A0001"));

            mockMvc.perform(get("/api/administrateurs/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));

            mockMvc.perform(get("/api/administrateurs/matricule/A0001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.matricule").value("A0001"));

            mockMvc.perform(get("/api/administrateurs/type/GLOBAL"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].typeAdmin").value("GLOBAL"));

            mockMvc.perform(get("/api/administrateurs/site/1"))
                    .andExpect(status().isOk());
        }

        @Test
        void penaltiesEndpoint_delegatesToService() throws Exception {
            PenaliteService service = mock(PenaliteService.class);

            PenaliteDTO dto = new PenaliteDTO(
                    1,
                    1,
                    1,
                    LocalDate.of(2026, 7, 1),
                    LocalDate.of(2026, 7, 8),
                    "Match privé incomplet",
                    true,
                    7,
                    LocalDate.of(2026, 7, 10),
                    LocalTime.of(10, 0),
                    LocalTime.of(11, 30),
                    "Terrain 1",
                    "Brussels Padel"
            );

            when(service.findActivePenaltiesForMember(1)).thenReturn(List.of(dto));

            MockMvc mockMvc = mvc(new PenaliteController(service));

            mockMvc.perform(get("/api/penalites/member/1/active"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].raison").value("Match privé incomplet"));
        }
    }

    private String siteJson() {
        return """
                {
                  "name": "Brussels Padel",
                  "city": "Bruxelles",
                  "adresse": "Rue du Test 1",
                  "codePostal": "1000",
                  "description": "Site",
                  "openingTime": "08:00:00",
                  "closingTime": "22:00:00",
                  "active": true,
                  "imageUrl": "/assets/site.jpg",
                  "courts": []
                }
                """;
    }

    private String courtJson() {
        return """
                {
                  "name": "Terrain 1",
                  "siteId": 1,
                  "indoor": true,
                  "active": true,
                  "maintenance": false
                }
                """;
    }

    private String horaireJson() {
        return """
                {
                  "siteId": 1,
                  "annee": 2026,
                  "heure_debut": "08:00:00",
                  "heure_fin": "22:00:00",
                  "duree_match_minutes": 90,
                  "pause_minutes": 15
                }
                """;
    }

    private String closureJson() {
        return """
                {
                  "siteId": 1,
                  "dateFermeture": "2026-07-21",
                  "raison": "Maintenance",
                  "global": false
                }
                """;
    }

    private SiteDTO siteDto(Integer id) {
        return new SiteDTO(
                id,
                "Brussels Padel",
                "Bruxelles",
                "Rue du Test 1",
                "1000",
                "Site",
                LocalTime.of(8, 0),
                LocalTime.of(22, 0),
                true,
                "/assets/site.jpg",
                List.of()
        );
    }
}