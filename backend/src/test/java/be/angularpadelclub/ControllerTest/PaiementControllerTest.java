package be.angularpadelclub.ControllerTest;

import be.angularpadelclub.Controler.PaiementController;
import be.angularpadelclub.DTO.MemberWalletDTO;
import be.angularpadelclub.DTO.PaiementDTO;
import be.angularpadelclub.Enum.PaiementMethode;
import be.angularpadelclub.Enum.PaiementProvider;
import be.angularpadelclub.Enum.PaiementStatut;
import be.angularpadelclub.Exception.GlobalExceptionHandler;
import be.angularpadelclub.Service.PaiementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaiementControllerTest {

    private PaiementService paiementService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        paiementService = mock(PaiementService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new PaiementController(paiementService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void findAll_returnsPayments() throws Exception {
        when(paiementService.findAll())
                .thenReturn(List.of(payment(1, PaiementStatut.EN_ATTENTE)));

        mockMvc.perform(get("/api/paiements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].statut").value("EN_ATTENTE"));
    }

    @Test
    void getWallet_returnsWallet() throws Exception {
        MemberWalletDTO wallet = new MemberWalletDTO(
                1,
                "G0001",
                "Jean Dupont",
                "EUR",
                1500,
                0,
                1500,
                0,
                0,
                -1500,
                List.of()
        );

        when(paiementService.getWallet(1)).thenReturn(wallet);

        mockMvc.perform(get("/api/paiements/member/1/wallet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(1))
                .andExpect(jsonPath("$.amountDueCentimes").value(1500));
    }

    @Test
    void initierPaiementReservation_returnsCreated() throws Exception {
        when(paiementService.initierPaiement(10))
                .thenReturn(payment(3, PaiementStatut.EN_ATTENTE));

        mockMvc.perform(post("/api/paiements/reservation/10/initier"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    void initierPaiementParticipation_returnsCreated() throws Exception {
        when(paiementService.initierPaiementPourParticipation(8))
                .thenReturn(payment(4, PaiementStatut.EN_ATTENTE));

        mockMvc.perform(post("/api/paiements/participation/8/initier"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.participationId").value(1));
    }

    @Test
    void initierPaiementDettes_returnsCreated() throws Exception {
        when(paiementService.initierPaiementDettesMembre(1))
                .thenReturn(payment(5, PaiementStatut.EN_ATTENTE));

        mockMvc.perform(post("/api/paiements/member/1/dettes/initier"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.membreId").value(1));
    }

    @Test
    void confirmerPaiement_returnsUpdatedPayment() throws Exception {
        when(paiementService.confirmerPaiement(1))
                .thenReturn(payment(1, PaiementStatut.VALIDE));

        mockMvc.perform(patch("/api/paiements/1/confirmer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("VALIDE"));
    }

    @Test
    void refuserAnnulerRembourser_delegateToService() throws Exception {
        when(paiementService.refuserPaiement(1))
                .thenReturn(payment(1, PaiementStatut.REFUSE));
        when(paiementService.annulerPaiement(2))
                .thenReturn(payment(2, PaiementStatut.ANNULE));
        when(paiementService.rembourserPaiement(3))
                .thenReturn(payment(3, PaiementStatut.REMBOURSE));

        mockMvc.perform(patch("/api/paiements/1/refuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("REFUSE"));

        mockMvc.perform(patch("/api/paiements/2/annuler"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("ANNULE"));

        mockMvc.perform(patch("/api/paiements/3/rembourser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("REMBOURSE"));
    }

    @Test
    void serviceException_isRenderedAsConflict() throws Exception {
        when(paiementService.confirmerPaiement(9))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Le paiement n'est pas en attente."
                ));

        mockMvc.perform(patch("/api/paiements/9/confirmer"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("pas en attente")));
    }

    private PaiementDTO payment(Integer id, PaiementStatut statut) {
        return new PaiementDTO(
                id,
                1,
                1,
                1,
                1500,
                "EUR",
                PaiementProvider.MOCK,
                "mock_" + id,
                "secret_" + id,
                PaiementMethode.CARTE,
                statut,
                LocalDateTime.of(2026, 7, 1, 10, 0),
                statut == PaiementStatut.VALIDE
                        ? LocalDateTime.of(2026, 7, 1, 10, 1)
                        : null,
                LocalDateTime.of(2026, 7, 1, 10, 15)
        );
    }
}