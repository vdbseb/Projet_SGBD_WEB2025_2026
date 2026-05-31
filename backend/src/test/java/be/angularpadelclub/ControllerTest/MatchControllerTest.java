package be.angularpadelclub.ControllerTest;

import be.angularpadelclub.Controler.MatchController;
import be.angularpadelclub.DTO.MatchDTO;
import be.angularpadelclub.Entity.MatchEntity;
import be.angularpadelclub.Entity.ParticipationEntity;
import be.angularpadelclub.Enum.MatchStatus;
import be.angularpadelclub.Enum.MatchType;
import be.angularpadelclub.Exception.GlobalExceptionHandler;
import be.angularpadelclub.Mapper.MatchMapper;
import be.angularpadelclub.Service.MatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MatchControllerTest {

    private MatchService matchService;
    private MatchMapper matchMapper;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        matchService = mock(MatchService.class);
        matchMapper = mock(MatchMapper.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new MatchController(matchService, matchMapper))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getAllMatches_mapsEveryMatch() throws Exception {
        MatchEntity match = new MatchEntity();

        when(matchService.findAll()).thenReturn(List.of(match));
        when(matchMapper.toDTO(match)).thenReturn(dto(1, MatchStatus.OUVERT));

        mockMvc.perform(get("/api/matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void annulerMatch_returnsCancelledMatch() throws Exception {
        MatchEntity match = new MatchEntity();

        when(matchService.annulerMatch(1, "G0001")).thenReturn(match);
        when(matchMapper.toDTO(match)).thenReturn(dto(1, MatchStatus.ANNULE));

        mockMvc.perform(patch("/api/matches/1/annuler")
                        .param("matricule", "G0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("ANNULE"));
    }

    @Test
    void joinMatch_returnsParticipationIdAnd201() throws Exception {
        ParticipationEntity participation = new ParticipationEntity();
        participation.setId(77);

        when(matchService.joinPublicMatch(1, 2)).thenReturn(participation);

        mockMvc.perform(post("/api/matches/1/join/2"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.participationId").value(77));
    }

    @Test
    void leaveMatch_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/matches/1/leave/2"))
                .andExpect(status().isNoContent());

        verify(matchService).leaveMatch(1, 2);
    }

    @Test
    void missingMatricule_returnsExplicitBadRequest() throws Exception {
        mockMvc.perform(patch("/api/matches/1/annuler"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("matricule")));
    }

    @Test
    void businessError_returnsServiceMessage() throws Exception {
        when(matchService.joinPublicMatch(1, 2))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Le match est complet."));

        mockMvc.perform(post("/api/matches/1/join/2"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Le match est complet."));
    }

    private MatchDTO dto(Integer id, MatchStatus status) {
        return new MatchDTO(
                id,
                1,
                "Terrain 1",
                1,
                LocalDate.of(2026, 7, 10),
                LocalTime.of(10, 0),
                LocalTime.of(11, 30),
                MatchType.PUBLIC,
                status,
                6000,
                List.of("G0001")
        );
    }
}