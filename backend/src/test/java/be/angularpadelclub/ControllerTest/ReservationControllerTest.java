package be.angularpadelclub.ControllerTest;

import be.angularpadelclub.Controler.ReservationController;
import be.angularpadelclub.DTO.ReservationDTO;
import be.angularpadelclub.Entity.ReservationEntity;
import be.angularpadelclub.Enum.MatchStatus;
import be.angularpadelclub.Enum.MatchType;
import be.angularpadelclub.Enum.ReservationStatus;
import be.angularpadelclub.Exception.GlobalExceptionHandler;
import be.angularpadelclub.Mapper.ReservationMapper;
import be.angularpadelclub.Service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReservationControllerTest {

    private ReservationService reservationService;
    private ReservationMapper reservationMapper;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        reservationService = mock(ReservationService.class);
        reservationMapper = mock(ReservationMapper.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new ReservationController(reservationService, reservationMapper))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void findAll_returnsReservations() throws Exception {
        when(reservationService.findAll()).thenReturn(List.of(dto(1)));

        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].matchType").value("PUBLIC"));
    }

    @Test
    void findById_returns404WhenReservationDoesNotExist() throws Exception {
        when(reservationService.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/reservations/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Réservation introuvable")));
    }

    @Test
    void addReservation_delegatesToServiceAndReturnsCreated() throws Exception {
        mockMvc.perform(post("/api/reservations")
                        .contentType("application/json")
                        .content("""
                                {
                                  "date":"2026-07-10",
                                  "endTime":"11:30:00",
                                  "startTime":"10:00:00",
                                  "courtId":1,
                                  "memberId":1,
                                  "matchType":"PUBLIC"
                                }
                                """))
                .andExpect(status().isCreated());

        verify(reservationService).addReservation(any(ReservationDTO.class));
    }

    @Test
    void addReservation_returns400WhenRequiredFieldsAreMissing() throws Exception {
        mockMvc.perform(post("/api/reservations")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("données invalides")));
    }

    @Test
    void findByCourtAndDate_mapsEntitiesToDto() throws Exception {
        ReservationEntity entity = new ReservationEntity();

        when(reservationService.findByCourtAndDate(1, LocalDate.of(2026, 7, 10)))
                .thenReturn(List.of(entity));
        when(reservationMapper.toDTOList(List.of(entity)))
                .thenReturn(List.of(dto(1)));

        mockMvc.perform(get("/api/reservations")
                        .param("courtId", "1")
                        .param("date", "2026-07-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courtId").value(1));
    }

    @Test
    void cancelReservation_delegatesToAdminWhenMemberIdMissing() throws Exception {
        mockMvc.perform(delete("/api/reservations/5"))
                .andExpect(status().isNoContent());

        verify(reservationService).cancelReservationByAdmin(5);
        verify(reservationService, never()).cancelReservation(anyInt(), anyInt());
    }

    @Test
    void cancelReservation_delegatesToMemberWhenMemberIdProvided() throws Exception {
        mockMvc.perform(delete("/api/reservations/5")
                        .param("memberId", "2"))
                .andExpect(status().isNoContent());

        verify(reservationService).cancelReservation(5, 2);
    }

    @Test
    void serviceException_isRenderedWithExplicitMessage() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "La réservation est déjà annulée."))
                .when(reservationService)
                .cancelReservationByAdmin(5);

        mockMvc.perform(delete("/api/reservations/5"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("La réservation est déjà annulée."));
    }

    private ReservationDTO dto(Integer id) {
        return new ReservationDTO(
                id,
                10,
                LocalDate.of(2026, 7, 10),
                LocalTime.of(11, 30),
                LocalTime.of(10, 0),
                1,
                "Brussels Padel",
                1,
                ReservationStatus.EN_ATTENTE_PAIEMENT,
                MatchType.PUBLIC,
                MatchStatus.OUVERT,
                List.of(),
                List.of()
        );
    }
}