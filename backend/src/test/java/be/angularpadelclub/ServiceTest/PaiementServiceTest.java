package be.angularpadelclub.ServiceTest;

import be.angularpadelclub.DTO.MemberWalletDTO;
import be.angularpadelclub.DTO.PaiementDTO;
import be.angularpadelclub.Entity.DetteMembreEntity;
import be.angularpadelclub.Entity.MatchEntity;
import be.angularpadelclub.Entity.MembreEntity;
import be.angularpadelclub.Entity.PaiementEntity;
import be.angularpadelclub.Entity.ParticipationEntity;
import be.angularpadelclub.Entity.ReservationEntity;
import be.angularpadelclub.Enum.DetteRaison;
import be.angularpadelclub.Enum.DetteStatut;
import be.angularpadelclub.Enum.MatchStatus;
import be.angularpadelclub.Enum.MatchType;
import be.angularpadelclub.Enum.PaiementMethode;
import be.angularpadelclub.Enum.PaiementProvider;
import be.angularpadelclub.Enum.PaiementStatut;
import be.angularpadelclub.Enum.ParticipationStatut;
import be.angularpadelclub.Enum.ReservationStatus;
import be.angularpadelclub.Mapper.DetteMembreMapper;
import be.angularpadelclub.Mapper.PaiementMapper;
import be.angularpadelclub.Repository.DetteMembreRepository;
import be.angularpadelclub.Repository.PaiementRepository;
import be.angularpadelclub.Repository.ParticipationRepository;
import be.angularpadelclub.Service.PaiementService;
import be.angularpadelclub.Service.ReferenceLookupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaiementServiceTest {

    @Mock
    private PaiementRepository paiementRepository;

    @Mock
    private ParticipationRepository participationRepository;

    @Mock
    private DetteMembreRepository detteMembreRepository;

    @Mock
    private ReferenceLookupService referenceLookupService;

    private PaiementService paiementService;

    @BeforeEach
    void setUp() {
        paiementService = new PaiementService(
                paiementRepository,
                participationRepository,
                detteMembreRepository,
                new PaiementMapper(),
                new DetteMembreMapper(),
                referenceLookupService
        );
    }

    @Test
    void initierPaiementPourParticipation_creeUnPaiementUniquementAvecLaPartMembreMemeSiDetteOuverte() {
        TestData data = buildData();

        DetteMembreEntity dette = buildDette(
                data.membre,
                data.participation,
                data.reservation,
                3000
        );

        when(referenceLookupService.findParticipationOrThrow(10))
                .thenReturn(data.participation);

        when(paiementRepository.existsByParticipation_IdAndStatut(
                10,
                PaiementStatut.EN_ATTENTE
        )).thenReturn(false);

        when(paiementRepository.saveAndFlush(any(PaiementEntity.class)))
                .thenAnswer(invocation -> {
                    PaiementEntity paiement = invocation.getArgument(0);
                    paiement.setId(100);
                    return paiement;
                });

        PaiementDTO result = paiementService.initierPaiementPourParticipation(10);

        assertEquals(1500, result.montantCentimes());
        assertEquals(PaiementStatut.EN_ATTENTE, result.statut());
        assertEquals(10, result.participationId());
        assertEquals(20, result.reservationId());
        assertEquals(1, result.membreId());

        assertEquals(DetteStatut.OUVERTE, dette.getStatut());

        verify(paiementRepository).saveAndFlush(argThat(paiement ->
                paiement.getMontantCentimes() == 1500
                        && paiement.getParticipation() == data.participation
                        && paiement.getReservation() == data.reservation
                        && paiement.getMembre() == data.membre
        ));

        verify(detteMembreRepository, never()).findByMembre_IdAndStatut(any(), any());
        verify(detteMembreRepository, never()).saveAll(any());
    }

    @Test
    void initierPaiementPourParticipation_refuseSiParticipationDejaPayee() {
        TestData data = buildData();
        data.participation.setStatut(ParticipationStatut.PAYEE);

        when(referenceLookupService.findParticipationOrThrow(10))
                .thenReturn(data.participation);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> paiementService.initierPaiementPourParticipation(10)
        );

        assertEquals(409, ex.getStatusCode().value());
        verify(paiementRepository, never()).saveAndFlush(any());
    }

    @Test
    void initierPaiementPourParticipation_refuseSiPaiementDejaEnAttente() {
        TestData data = buildData();

        when(referenceLookupService.findParticipationOrThrow(10))
                .thenReturn(data.participation);

        when(paiementRepository.existsByParticipation_IdAndStatut(
                10,
                PaiementStatut.EN_ATTENTE
        )).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> paiementService.initierPaiementPourParticipation(10)
        );

        assertEquals(409, ex.getStatusCode().value());
        verify(paiementRepository, never()).saveAndFlush(any());
    }

    @Test
    void initierPaiementPourParticipation_refuseSiParticipationLibereeOuAnnulee() {
        TestData dataLiberee = buildData();
        dataLiberee.participation.setStatut(ParticipationStatut.LIBEREE);

        when(referenceLookupService.findParticipationOrThrow(10))
                .thenReturn(dataLiberee.participation);

        ResponseStatusException exLiberee = assertThrows(
                ResponseStatusException.class,
                () -> paiementService.initierPaiementPourParticipation(10)
        );

        assertEquals(409, exLiberee.getStatusCode().value());
        verify(paiementRepository, never()).saveAndFlush(any());

        reset(referenceLookupService, paiementRepository);

        TestData dataAnnulee = buildData();
        dataAnnulee.participation.setStatut(ParticipationStatut.ANNULEE);

        when(referenceLookupService.findParticipationOrThrow(10))
                .thenReturn(dataAnnulee.participation);

        ResponseStatusException exAnnulee = assertThrows(
                ResponseStatusException.class,
                () -> paiementService.initierPaiementPourParticipation(10)
        );

        assertEquals(409, exAnnulee.getStatusCode().value());
        verify(paiementRepository, never()).saveAndFlush(any());
    }

    @Test
    void initierPaiementPourParticipation_refuseSiParticipationSansMembre() {
        TestData data = buildData();
        data.participation.setMembre(null);

        when(referenceLookupService.findParticipationOrThrow(10))
                .thenReturn(data.participation);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> paiementService.initierPaiementPourParticipation(10)
        );

        assertEquals(409, ex.getStatusCode().value());
        verify(paiementRepository, never()).saveAndFlush(any());
    }

    @Test
    void initierPaiementPourParticipation_refuseSiParticipationSansMatch() {
        TestData data = buildData();
        data.participation.setMatch(null);

        when(referenceLookupService.findParticipationOrThrow(10))
                .thenReturn(data.participation);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> paiementService.initierPaiementPourParticipation(10)
        );

        assertEquals(409, ex.getStatusCode().value());
        verify(paiementRepository, never()).saveAndFlush(any());
    }

    @Test
    void initierPaiementPourParticipation_refuseSiParticipationSansReservation() {
        TestData data = buildData();
        data.match.setReservation(null);

        when(referenceLookupService.findParticipationOrThrow(10))
                .thenReturn(data.participation);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> paiementService.initierPaiementPourParticipation(10)
        );

        assertEquals(409, ex.getStatusCode().value());
        verify(paiementRepository, never()).saveAndFlush(any());
    }

    @Test
    void initierPaiement_refuseSiReservationInexistante() {
        when(referenceLookupService.findReservationOrThrow(999))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Réservation introuvable avec l'id : 999"
                ));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> paiementService.initierPaiement(999)
        );

        assertEquals(404, ex.getStatusCode().value());
        verify(participationRepository, never()).findByMatch_IdAndMembre_Id(any(), any());
        verify(paiementRepository, never()).saveAndFlush(any());
    }

    @Test
    void initierPaiement_refuseSiParticipationOrganisateurIntrouvable() {
        TestData data = buildData();

        when(referenceLookupService.findReservationOrThrow(20))
                .thenReturn(data.reservation);

        when(participationRepository.findByMatch_IdAndMembre_Id(5, 1))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> paiementService.initierPaiement(20)
        );

        assertEquals(404, ex.getStatusCode().value());
        verify(paiementRepository, never()).saveAndFlush(any());
    }

    @Test
    void initierPaiement_appellePaiementParticipationOrganisateur() {
        TestData data = buildData();

        when(referenceLookupService.findReservationOrThrow(20))
                .thenReturn(data.reservation);

        when(participationRepository.findByMatch_IdAndMembre_Id(5, 1))
                .thenReturn(Optional.of(data.participation));

        when(referenceLookupService.findParticipationOrThrow(10))
                .thenReturn(data.participation);

        when(paiementRepository.existsByParticipation_IdAndStatut(
                10,
                PaiementStatut.EN_ATTENTE
        )).thenReturn(false);

        when(paiementRepository.saveAndFlush(any(PaiementEntity.class)))
                .thenAnswer(invocation -> {
                    PaiementEntity paiement = invocation.getArgument(0);
                    paiement.setId(100);
                    return paiement;
                });

        PaiementDTO result = paiementService.initierPaiement(20);

        assertEquals(1500, result.montantCentimes());
        assertEquals(10, result.participationId());
        assertEquals(20, result.reservationId());
    }

    @Test
    void initierPaiementDettesMembre_creeUnPaiementAvecLaSommeDesDettesOuvertes() {
        TestData data = buildData();

        DetteMembreEntity dette1 = buildDette(
                data.membre,
                data.participation,
                data.reservation,
                3000
        );
        dette1.setId(201);

        DetteMembreEntity dette2 = buildDette(
                data.membre,
                data.participation,
                data.reservation,
                1500
        );
        dette2.setId(202);

        when(referenceLookupService.findMembreOrThrow(1))
                .thenReturn(data.membre);

        when(detteMembreRepository.findByMembre_IdAndStatut(1, DetteStatut.OUVERTE))
                .thenReturn(List.of(dette1, dette2));

        when(paiementRepository.saveAndFlush(any(PaiementEntity.class)))
                .thenAnswer(invocation -> {
                    PaiementEntity paiement = invocation.getArgument(0);
                    paiement.setId(100);
                    return paiement;
                });

        when(referenceLookupService.findPaiementOrThrow(100))
                .thenAnswer(invocation -> {
                    PaiementEntity paiement = new PaiementEntity();
                    paiement.setId(100);
                    paiement.setMembre(data.membre);
                    paiement.setMontantCentimes(4500);
                    paiement.setDevise("EUR");
                    paiement.setProvider(PaiementProvider.MOCK);
                    paiement.setMethode(PaiementMethode.CARTE);
                    paiement.setStatut(PaiementStatut.EN_ATTENTE);
                    paiement.setDateCreation(LocalDateTime.now());
                    return paiement;
                });

        PaiementDTO result = paiementService.initierPaiementDettesMembre(1);

        assertEquals(4500, result.montantCentimes());
        assertEquals(1, result.membreId());
        assertNull(result.reservationId());
        assertNull(result.participationId());
        assertEquals(PaiementStatut.EN_ATTENTE, result.statut());

        verify(paiementRepository).saveAndFlush(argThat(paiement ->
                paiement.getMembre() == data.membre
                        && paiement.getReservation() == null
                        && paiement.getParticipation() == null
                        && paiement.getMontantCentimes() == 4500
        ));
    }

    @Test
    void initierPaiementDettesMembre_refuseSiMembreInexistant() {
        when(referenceLookupService.findMembreOrThrow(999))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Membre introuvable avec l'id : 999"
                ));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> paiementService.initierPaiementDettesMembre(999)
        );

        assertEquals(404, ex.getStatusCode().value());
        verify(paiementRepository, never()).saveAndFlush(any());
    }

    @Test
    void initierPaiementDettesMembre_refuseSiAucuneDetteOuverte() {
        TestData data = buildData();

        when(referenceLookupService.findMembreOrThrow(1))
                .thenReturn(data.membre);

        when(detteMembreRepository.findByMembre_IdAndStatut(1, DetteStatut.OUVERTE))
                .thenReturn(List.of());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> paiementService.initierPaiementDettesMembre(1)
        );

        assertEquals(409, ex.getStatusCode().value());
        verify(paiementRepository, never()).saveAndFlush(any());
    }

    @Test
    void initierPaiementDettesMembre_refuseSiMontantTotalNonPositif() {
        TestData data = buildData();

        DetteMembreEntity dette = buildDette(
                data.membre,
                data.participation,
                data.reservation,
                0
        );

        when(referenceLookupService.findMembreOrThrow(1))
                .thenReturn(data.membre);

        when(detteMembreRepository.findByMembre_IdAndStatut(1, DetteStatut.OUVERTE))
                .thenReturn(List.of(dette));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> paiementService.initierPaiementDettesMembre(1)
        );

        assertEquals(409, ex.getStatusCode().value());
        verify(paiementRepository, never()).saveAndFlush(any());
    }

    @Test
    void confirmerPaiement_validePaiementParticipationReservationSansFermerDettes() {
        TestData data = buildData();

        PaiementEntity paiement = buildPaiement(
                100,
                data.membre,
                data.reservation,
                data.participation,
                1500,
                PaiementStatut.EN_ATTENTE
        );

        DetteMembreEntity dette = buildDette(
                data.membre,
                data.participation,
                data.reservation,
                3000
        );

        when(referenceLookupService.findPaiementOrThrow(100))
                .thenReturn(paiement);

        when(paiementRepository.saveAndFlush(paiement))
                .thenReturn(paiement);

        PaiementDTO result = paiementService.confirmerPaiement(100);

        assertEquals(PaiementStatut.VALIDE, result.statut());
        assertNotNull(result.datePaiement());
        assertEquals(ParticipationStatut.PAYEE, data.participation.getStatut());
        assertEquals(ReservationStatus.VALIDEE, data.reservation.getStatut());

        assertEquals(DetteStatut.OUVERTE, dette.getStatut());
        assertNull(dette.getDateResolution());

        verify(detteMembreRepository, never()).findByMembre_IdAndStatut(any(), any());
        verify(detteMembreRepository, never()).saveAll(any());
    }

    @Test
    void confirmerPaiement_paiementDettesFermeLesDettesOuvertes() {
        TestData data = buildData();

        DetteMembreEntity dette1 = buildDette(
                data.membre,
                data.participation,
                data.reservation,
                3000
        );
        dette1.setId(201);

        DetteMembreEntity dette2 = buildDette(
                data.membre,
                data.participation,
                data.reservation,
                1500
        );
        dette2.setId(202);

        PaiementEntity paiement = buildPaiement(
                100,
                data.membre,
                null,
                null,
                4500,
                PaiementStatut.EN_ATTENTE
        );

        when(referenceLookupService.findPaiementOrThrow(100))
                .thenReturn(paiement);

        when(detteMembreRepository.findByMembre_IdAndStatut(1, DetteStatut.OUVERTE))
                .thenReturn(List.of(dette1, dette2));

        when(paiementRepository.saveAndFlush(paiement))
                .thenReturn(paiement);

        PaiementDTO result = paiementService.confirmerPaiement(100);

        assertEquals(PaiementStatut.VALIDE, result.statut());
        assertNotNull(result.datePaiement());

        assertEquals(DetteStatut.PAYEE, dette1.getStatut());
        assertEquals(DetteStatut.PAYEE, dette2.getStatut());
        assertNotNull(dette1.getDateResolution());
        assertNotNull(dette2.getDateResolution());

        verify(detteMembreRepository).saveAll(List.of(dette1, dette2));
    }

    @Test
    void confirmerPaiement_refuseSiPaiementPasEnAttente() {
        TestData data = buildData();

        PaiementEntity paiement = buildPaiement(
                100,
                data.membre,
                data.reservation,
                data.participation,
                1500,
                PaiementStatut.REFUSE
        );

        when(referenceLookupService.findPaiementOrThrow(100))
                .thenReturn(paiement);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> paiementService.confirmerPaiement(100)
        );

        assertEquals(409, ex.getStatusCode().value());
        verify(paiementRepository, never()).saveAndFlush(any());
    }

    @Test
    void confirmerPaiement_retourneDirectementSiDejaValide() {
        TestData data = buildData();

        PaiementEntity paiement = buildPaiement(
                100,
                data.membre,
                data.reservation,
                data.participation,
                1500,
                PaiementStatut.VALIDE
        );

        LocalDateTime datePaiement = LocalDateTime.of(2026, 5, 29, 10, 30);
        paiement.setDatePaiement(datePaiement);

        when(referenceLookupService.findPaiementOrThrow(100))
                .thenReturn(paiement);

        PaiementDTO result = paiementService.confirmerPaiement(100);

        assertEquals(PaiementStatut.VALIDE, result.statut());
        assertEquals(datePaiement, result.datePaiement());

        verify(paiementRepository, never()).saveAndFlush(any());
        verify(detteMembreRepository, never()).findByMembre_IdAndStatut(any(), any());
    }

    @Test
    void confirmerPaiement_nePlantePasSiReservationEstNull() {
        TestData data = buildData();

        PaiementEntity paiement = buildPaiement(
                100,
                data.membre,
                null,
                data.participation,
                1500,
                PaiementStatut.EN_ATTENTE
        );

        when(referenceLookupService.findPaiementOrThrow(100))
                .thenReturn(paiement);

        when(paiementRepository.saveAndFlush(paiement))
                .thenReturn(paiement);

        PaiementDTO result = paiementService.confirmerPaiement(100);

        assertEquals(PaiementStatut.VALIDE, result.statut());
        assertEquals(ParticipationStatut.PAYEE, data.participation.getStatut());

        verify(paiementRepository).saveAndFlush(paiement);
        verify(detteMembreRepository, never()).findByMembre_IdAndStatut(any(), any());
    }

    @Test
    void confirmerPaiement_neValidePasReservationSiParticipationEstNull() {
        TestData data = buildData();

        PaiementEntity paiement = buildPaiement(
                100,
                data.membre,
                data.reservation,
                null,
                1500,
                PaiementStatut.EN_ATTENTE
        );

        when(referenceLookupService.findPaiementOrThrow(100))
                .thenReturn(paiement);

        when(paiementRepository.saveAndFlush(paiement))
                .thenReturn(paiement);

        PaiementDTO result = paiementService.confirmerPaiement(100);

        assertEquals(PaiementStatut.VALIDE, result.statut());
        assertEquals(ReservationStatus.EN_ATTENTE_PAIEMENT, data.reservation.getStatut());

        verify(paiementRepository).saveAndFlush(paiement);
        verify(detteMembreRepository, never()).findByMembre_IdAndStatut(any(), any());
    }

    @Test
    void refuserPaiement_passePaiementEnRefuse() {
        TestData data = buildData();

        PaiementEntity paiement = buildPaiement(
                100,
                data.membre,
                data.reservation,
                data.participation,
                1500,
                PaiementStatut.EN_ATTENTE
        );

        paiement.setDateExpiration(LocalDateTime.now().plusMinutes(15));

        when(referenceLookupService.findPaiementOrThrow(100))
                .thenReturn(paiement);

        when(paiementRepository.saveAndFlush(paiement))
                .thenReturn(paiement);

        PaiementDTO result = paiementService.refuserPaiement(100);

        assertEquals(PaiementStatut.REFUSE, result.statut());
        assertNull(paiement.getDateExpiration());

        verify(paiementRepository).saveAndFlush(paiement);
    }

    @Test
    void annulerPaiement_passePaiementEnAnnule() {
        TestData data = buildData();

        PaiementEntity paiement = buildPaiement(
                100,
                data.membre,
                data.reservation,
                data.participation,
                1500,
                PaiementStatut.EN_ATTENTE
        );

        paiement.setDateExpiration(LocalDateTime.now().plusMinutes(15));

        when(referenceLookupService.findPaiementOrThrow(100))
                .thenReturn(paiement);

        when(paiementRepository.saveAndFlush(paiement))
                .thenReturn(paiement);

        PaiementDTO result = paiementService.annulerPaiement(100);

        assertEquals(PaiementStatut.ANNULE, result.statut());
        assertNull(paiement.getDateExpiration());

        verify(paiementRepository).saveAndFlush(paiement);
    }

    @Test
    void rembourserPaiement_passeUnPaiementValideEnRembourseEtAnnuleParticipation() {
        TestData data = buildData();

        PaiementEntity paiement = buildPaiement(
                100,
                data.membre,
                data.reservation,
                data.participation,
                1500,
                PaiementStatut.VALIDE
        );

        when(referenceLookupService.findPaiementOrThrow(100))
                .thenReturn(paiement);

        when(paiementRepository.saveAndFlush(paiement))
                .thenReturn(paiement);

        PaiementDTO result = paiementService.rembourserPaiement(100);

        assertEquals(PaiementStatut.REMBOURSE, result.statut());
        assertEquals(ParticipationStatut.ANNULEE, data.participation.getStatut());

        verify(paiementRepository).saveAndFlush(paiement);
    }

    @Test
    void rembourserPaiement_refusePaiementDeDettes() {
        TestData data = buildData();

        PaiementEntity paiement = buildPaiement(
                100,
                data.membre,
                null,
                null,
                4500,
                PaiementStatut.VALIDE
        );

        when(referenceLookupService.findPaiementOrThrow(100))
                .thenReturn(paiement);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> paiementService.rembourserPaiement(100)
        );

        assertEquals(409, ex.getStatusCode().value());
        assertEquals(PaiementStatut.VALIDE, paiement.getStatut());

        verify(paiementRepository, never()).saveAndFlush(any());
    }

    @Test
    void rembourserPaiement_refuseSiPaiementNonValide() {
        TestData data = buildData();

        PaiementEntity paiement = buildPaiement(
                100,
                data.membre,
                data.reservation,
                data.participation,
                1500,
                PaiementStatut.EN_ATTENTE
        );

        when(referenceLookupService.findPaiementOrThrow(100))
                .thenReturn(paiement);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> paiementService.rembourserPaiement(100)
        );

        assertEquals(409, ex.getStatusCode().value());
        verify(paiementRepository, never()).saveAndFlush(any());
    }

    @Test
    void rembourserPaiementsReservation_remboursePaiementsValidesEtAnnuleParticipations() {
        TestData data = buildData();

        ParticipationEntity autreParticipation = buildParticipation(
                11,
                data.membre,
                data.match,
                ParticipationStatut.PAYEE
        );

        PaiementEntity paiement1 = buildPaiement(
                101,
                data.membre,
                data.reservation,
                data.participation,
                1500,
                PaiementStatut.VALIDE
        );

        PaiementEntity paiement2 = buildPaiement(
                102,
                data.membre,
                data.reservation,
                autreParticipation,
                1500,
                PaiementStatut.VALIDE
        );

        when(paiementRepository.findByReservation_IdAndStatut(20, PaiementStatut.VALIDE))
                .thenReturn(List.of(paiement1, paiement2));

        paiementService.rembourserPaiementsReservation(data.reservation);

        assertEquals(PaiementStatut.REMBOURSE, paiement1.getStatut());
        assertEquals(PaiementStatut.REMBOURSE, paiement2.getStatut());
        assertEquals(ParticipationStatut.ANNULEE, data.participation.getStatut());
        assertEquals(ParticipationStatut.ANNULEE, autreParticipation.getStatut());

        verify(paiementRepository).saveAllAndFlush(List.of(paiement1, paiement2));
    }

    @Test
    void rembourserPaiementsReservation_ignoreReservationNull() {
        paiementService.rembourserPaiementsReservation(null);

        verify(paiementRepository, never())
                .findByReservation_IdAndStatut(any(), any());
    }

    @Test
    void rembourserPaiementsReservation_ignoreReservationSansId() {
        ReservationEntity reservation = new ReservationEntity();

        paiementService.rembourserPaiementsReservation(reservation);

        verify(paiementRepository, never())
                .findByReservation_IdAndStatut(any(), any());
    }

    @Test
    void rembourserPaiementsMatch_remboursePaiementsValidesEtAnnuleParticipations() {
        TestData data = buildData();

        ParticipationEntity autreParticipation = buildParticipation(
                11,
                data.membre,
                data.match,
                ParticipationStatut.PAYEE
        );

        PaiementEntity paiement1 = buildPaiement(
                101,
                data.membre,
                data.reservation,
                data.participation,
                1500,
                PaiementStatut.VALIDE
        );

        PaiementEntity paiement2 = buildPaiement(
                102,
                data.membre,
                data.reservation,
                autreParticipation,
                1500,
                PaiementStatut.VALIDE
        );

        when(paiementRepository.findByParticipation_Match_IdAndStatut(
                5,
                PaiementStatut.VALIDE
        )).thenReturn(List.of(paiement1, paiement2));

        paiementService.rembourserPaiementsMatch(5);

        assertEquals(PaiementStatut.REMBOURSE, paiement1.getStatut());
        assertEquals(PaiementStatut.REMBOURSE, paiement2.getStatut());
        assertEquals(ParticipationStatut.ANNULEE, data.participation.getStatut());
        assertEquals(ParticipationStatut.ANNULEE, autreParticipation.getStatut());

        verify(paiementRepository).saveAllAndFlush(List.of(paiement1, paiement2));
    }

    @Test
    void rembourserPaiementsMatch_ignoreMatchIdNull() {
        paiementService.rembourserPaiementsMatch(null);

        verify(paiementRepository, never())
                .findByParticipation_Match_IdAndStatut(any(), any());
    }

    @Test
    void rembourserPaiementsParticipation_rembourseValidesEtAnnuleEnAttente() {
        TestData data = buildData();

        PaiementEntity paiementValide = buildPaiement(
                101,
                data.membre,
                data.reservation,
                data.participation,
                1500,
                PaiementStatut.VALIDE
        );

        PaiementEntity paiementEnAttente = buildPaiement(
                102,
                data.membre,
                data.reservation,
                data.participation,
                1500,
                PaiementStatut.EN_ATTENTE
        );

        paiementEnAttente.setDateExpiration(LocalDateTime.now().plusMinutes(15));

        when(paiementRepository.findByParticipation_IdAndStatut(
                10,
                PaiementStatut.VALIDE
        )).thenReturn(List.of(paiementValide));

        when(paiementRepository.findByParticipation_IdAndStatut(
                10,
                PaiementStatut.EN_ATTENTE
        )).thenReturn(List.of(paiementEnAttente));

        paiementService.rembourserPaiementsParticipation(10);

        assertEquals(PaiementStatut.REMBOURSE, paiementValide.getStatut());
        assertEquals(ParticipationStatut.ANNULEE, data.participation.getStatut());
        assertEquals(PaiementStatut.ANNULE, paiementEnAttente.getStatut());
        assertNull(paiementEnAttente.getDateExpiration());

        verify(paiementRepository).saveAllAndFlush(List.of(paiementValide));
        verify(paiementRepository).saveAllAndFlush(List.of(paiementEnAttente));
    }

    @Test
    void rembourserPaiementsParticipation_ignoreParticipationIdNull() {
        paiementService.rembourserPaiementsParticipation(null);

        verify(paiementRepository, never())
                .findByParticipation_IdAndStatut(any(), any());
    }

    @Test
    void createDebt_creeDetteOuverte() {
        TestData data = buildData();

        when(detteMembreRepository.existsByParticipation_IdAndStatut(
                10,
                DetteStatut.OUVERTE
        )).thenReturn(false);

        paiementService.createDebt(
                data.membre,
                data.participation,
                data.reservation,
                1500,
                DetteRaison.PARTICIPATION_IMPAYEE
        );

        ArgumentCaptor<DetteMembreEntity> captor =
                ArgumentCaptor.forClass(DetteMembreEntity.class);

        verify(detteMembreRepository).save(captor.capture());

        DetteMembreEntity saved = captor.getValue();

        assertEquals(data.membre, saved.getMembre());
        assertEquals(data.participation, saved.getParticipation());
        assertEquals(data.reservation, saved.getReservation());
        assertEquals(1500, saved.getMontantCentimes());
        assertEquals(DetteRaison.PARTICIPATION_IMPAYEE, saved.getRaison());
        assertEquals(DetteStatut.OUVERTE, saved.getStatut());
        assertNotNull(saved.getDateCreation());
    }

    @Test
    void createDebt_neCreePasDeDetteSiDetteOuverteExisteDeja() {
        TestData data = buildData();

        when(detteMembreRepository.existsByParticipation_IdAndStatut(
                10,
                DetteStatut.OUVERTE
        )).thenReturn(true);

        paiementService.createDebt(
                data.membre,
                data.participation,
                data.reservation,
                1500,
                DetteRaison.PARTICIPATION_IMPAYEE
        );

        verify(detteMembreRepository, never()).save(any());
    }

    @Test
    void createDebt_ignoreSiMembreNull() {
        TestData data = buildData();

        paiementService.createDebt(
                null,
                data.participation,
                data.reservation,
                1500,
                DetteRaison.PARTICIPATION_IMPAYEE
        );

        verify(detteMembreRepository, never()).save(any());
    }

    @Test
    void createDebt_metMontantZeroSiMontantNull() {
        TestData data = buildData();

        when(detteMembreRepository.existsByParticipation_IdAndStatut(
                10,
                DetteStatut.OUVERTE
        )).thenReturn(false);

        paiementService.createDebt(
                data.membre,
                data.participation,
                data.reservation,
                null,
                DetteRaison.PARTICIPATION_IMPAYEE
        );

        ArgumentCaptor<DetteMembreEntity> captor =
                ArgumentCaptor.forClass(DetteMembreEntity.class);

        verify(detteMembreRepository).save(captor.capture());

        assertEquals(0, captor.getValue().getMontantCentimes());
    }

    @Test
    void createDebt_creeDetteSansParticipation() {
        TestData data = buildData();

        paiementService.createDebt(
                data.membre,
                null,
                data.reservation,
                4500,
                DetteRaison.SOLDE_ORGANISATEUR
        );

        ArgumentCaptor<DetteMembreEntity> captor =
                ArgumentCaptor.forClass(DetteMembreEntity.class);

        verify(detteMembreRepository).save(captor.capture());

        DetteMembreEntity saved = captor.getValue();

        assertEquals(data.membre, saved.getMembre());
        assertNull(saved.getParticipation());
        assertEquals(data.reservation, saved.getReservation());
        assertEquals(4500, saved.getMontantCentimes());
        assertEquals(DetteRaison.SOLDE_ORGANISATEUR, saved.getRaison());
        assertEquals(DetteStatut.OUVERTE, saved.getStatut());

        verify(detteMembreRepository, never()).existsByParticipation_IdAndStatut(any(), any());
    }

    @Test
    void getWallet_calculeLesTotauxDuMembre() {
        TestData data = buildData();

        DetteMembreEntity dette = buildDette(
                data.membre,
                data.participation,
                data.reservation,
                3000
        );

        PaiementEntity paiementEnAttente = buildPaiement(
                101,
                data.membre,
                data.reservation,
                data.participation,
                1500,
                PaiementStatut.EN_ATTENTE
        );

        PaiementEntity paiementValide = buildPaiement(
                102,
                data.membre,
                data.reservation,
                data.participation,
                1500,
                PaiementStatut.VALIDE
        );

        PaiementEntity paiementRembourse = buildPaiement(
                103,
                data.membre,
                data.reservation,
                data.participation,
                1500,
                PaiementStatut.REMBOURSE
        );

        when(referenceLookupService.findMembreOrThrow(1))
                .thenReturn(data.membre);

        when(detteMembreRepository.findByMembre_IdAndStatut(1, DetteStatut.OUVERTE))
                .thenReturn(List.of(dette));

        when(paiementRepository.findByMembre_Id(1))
                .thenReturn(List.of(
                        paiementEnAttente,
                        paiementValide,
                        paiementRembourse
                ));

        MemberWalletDTO result = paiementService.getWallet(1);

        assertEquals(3000, result.amountDueCentimes());
        assertEquals(1500, result.amountPendingCentimes());
        assertEquals(1500, result.amountPaidCentimes());
        assertEquals(1500, result.amountRefundedCentimes());
        assertEquals(-3000, result.balanceCentimes());
        assertEquals(1, result.openDebts().size());
    }

    @Test
    void remboursementParticipationNeRemboursePasLesDettesOuvertesDuMembre() {
        TestData data = buildData();

        DetteMembreEntity detteExistante = buildDette(
                data.membre,
                data.participation,
                data.reservation,
                3000
        );

        AtomicReference<PaiementEntity> paiementSauve =
                new AtomicReference<>();

        when(referenceLookupService.findParticipationOrThrow(10))
                .thenReturn(data.participation);

        when(paiementRepository.existsByParticipation_IdAndStatut(
                10,
                PaiementStatut.EN_ATTENTE
        )).thenReturn(false);

        when(paiementRepository.saveAndFlush(any(PaiementEntity.class)))
                .thenAnswer(invocation -> {
                    PaiementEntity paiement = invocation.getArgument(0);

                    if (paiement.getId() == null) {
                        paiement.setId(100);
                    }

                    paiementSauve.set(paiement);
                    return paiement;
                });

        PaiementDTO paiementCree =
                paiementService.initierPaiementPourParticipation(10);

        assertEquals(1500, paiementCree.montantCentimes());
        assertEquals(PaiementStatut.EN_ATTENTE, paiementCree.statut());
        assertEquals(DetteStatut.OUVERTE, detteExistante.getStatut());
        assertNull(detteExistante.getDateResolution());

        PaiementEntity paiement = paiementSauve.get();

        when(referenceLookupService.findPaiementOrThrow(100))
                .thenReturn(paiement);

        PaiementDTO paiementConfirme =
                paiementService.confirmerPaiement(100);

        assertEquals(PaiementStatut.VALIDE, paiementConfirme.statut());
        assertEquals(1500, paiementConfirme.montantCentimes());
        assertEquals(ParticipationStatut.PAYEE, data.participation.getStatut());
        assertEquals(ReservationStatus.VALIDEE, data.reservation.getStatut());

        assertEquals(DetteStatut.OUVERTE, detteExistante.getStatut());
        assertNull(detteExistante.getDateResolution());

        PaiementDTO paiementRembourse =
                paiementService.rembourserPaiement(100);

        assertEquals(PaiementStatut.REMBOURSE, paiementRembourse.statut());
        assertEquals(1500, paiementRembourse.montantCentimes());
        assertEquals(ParticipationStatut.ANNULEE, data.participation.getStatut());

        assertEquals(DetteStatut.OUVERTE, detteExistante.getStatut());
        assertNull(detteExistante.getDateResolution());

        verify(detteMembreRepository, never()).findByMembre_IdAndStatut(any(), any());
        verify(detteMembreRepository, never()).saveAll(any());
    }

    @Test
    void getWallet_refuseSiMembreInexistant() {
        when(referenceLookupService.findMembreOrThrow(999))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Membre introuvable avec l'id : 999"
                ));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> paiementService.getWallet(999)
        );

        assertEquals(404, ex.getStatusCode().value());
    }

    private TestData buildData() {
        MembreEntity membre = new MembreEntity();

        membre.setId(1);
        membre.setMatricule("G0001");
        membre.setPrenom("Sebastien");
        membre.setNom("Test");
        membre.setEmail("seb@test.be");
        membre.setActif(true);

        ReservationEntity reservation = new ReservationEntity();

        reservation.setId(20);
        reservation.setDate(LocalDate.of(2026, 6, 1));
        reservation.setStartTime(LocalTime.of(18, 0));
        reservation.setEndTime(LocalTime.of(19, 30));
        reservation.setMember(membre);
        reservation.setStatut(ReservationStatus.EN_ATTENTE_PAIEMENT);

        MatchEntity match = new MatchEntity();

        match.setId(5);
        match.setReservation(reservation);
        match.setOrganisateur(membre);
        match.setDateMatch(reservation.getDate());
        match.setHeureDebut(reservation.getStartTime());
        match.setHeureFin(reservation.getEndTime());
        match.setTypeMatch(MatchType.PUBLIC);
        match.setStatut(MatchStatus.OUVERT);
        match.setPrixTotal(60);

        reservation.setMatch(match);

        ParticipationEntity participation = buildParticipation(
                10,
                membre,
                match,
                ParticipationStatut.EN_ATTENTE_PAIEMENT
        );

        return new TestData(
                membre,
                reservation,
                match,
                participation
        );
    }

    private ParticipationEntity buildParticipation(
            Integer id,
            MembreEntity membre,
            MatchEntity match,
            ParticipationStatut statut
    ) {
        ParticipationEntity participation = new ParticipationEntity();

        participation.setId(id);
        participation.setMembre(membre);
        participation.setMatch(match);
        participation.setStatut(statut);
        participation.setMontantDuCentimes(1500);
        participation.setDateInscription(LocalDateTime.now());

        return participation;
    }

    private PaiementEntity buildPaiement(
            Integer id,
            MembreEntity membre,
            ReservationEntity reservation,
            ParticipationEntity participation,
            Integer montantCentimes,
            PaiementStatut statut
    ) {
        PaiementEntity paiement = new PaiementEntity();

        paiement.setId(id);
        paiement.setMembre(membre);
        paiement.setReservation(reservation);
        paiement.setParticipation(participation);
        paiement.setMontantCentimes(montantCentimes);
        paiement.setDevise("EUR");
        paiement.setProvider(PaiementProvider.MOCK);
        paiement.setMethode(PaiementMethode.CARTE);
        paiement.setProviderPaymentId("mock_pi_" + id);
        paiement.setClientSecret("mock_secret_" + id);
        paiement.setStatut(statut);
        paiement.setDateCreation(LocalDateTime.now());

        return paiement;
    }

    private DetteMembreEntity buildDette(
            MembreEntity membre,
            ParticipationEntity participation,
            ReservationEntity reservation,
            Integer montantCentimes
    ) {
        DetteMembreEntity dette = new DetteMembreEntity();

        dette.setId(200);
        dette.setMembre(membre);
        dette.setParticipation(participation);
        dette.setReservation(reservation);
        dette.setMontantCentimes(montantCentimes);
        dette.setStatut(DetteStatut.OUVERTE);
        dette.setRaison(DetteRaison.SOLDE_ORGANISATEUR);
        dette.setDateCreation(LocalDateTime.now());

        return dette;
    }

    private record TestData(
            MembreEntity membre,
            ReservationEntity reservation,
            MatchEntity match,
            ParticipationEntity participation
    ) {
    }
}