package be.angularpadelclub.ServiceTest;

import be.angularpadelclub.Entity.MatchEntity;
import be.angularpadelclub.Entity.MembreEntity;
import be.angularpadelclub.Entity.ParticipationEntity;
import be.angularpadelclub.Entity.PenaliteEntity;
import be.angularpadelclub.Entity.ReservationEntity;
import be.angularpadelclub.Enum.DetteRaison;
import be.angularpadelclub.Enum.MatchStatus;
import be.angularpadelclub.Enum.MatchType;
import be.angularpadelclub.Enum.ParticipationStatut;
import be.angularpadelclub.Repository.MatchRepository;
import be.angularpadelclub.Repository.ParticipationRepository;
import be.angularpadelclub.Repository.PenaliteRepository;
import be.angularpadelclub.Service.MatchBillingService;
import be.angularpadelclub.Service.MatchSchedulerService;
import be.angularpadelclub.Service.PaiementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchSchedulerServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private ParticipationRepository participationRepository;

    @Mock
    private PenaliteRepository penaliteRepository;

    @Mock
    private PaiementService paiementService;

    @Mock
    private MatchBillingService matchBillingService;

    private MatchSchedulerService matchSchedulerService;

    @BeforeEach
    void setUp() {
        matchSchedulerService = new MatchSchedulerService(
                matchRepository,
                participationRepository,
                penaliteRepository,
                paiementService,
                matchBillingService
        );
    }

    @Nested
    @DisplayName("Conversion des matchs privés incomplets en publics")
    class ConvertirMatchsPrivesIncompletsEnPublics {

        @Test
        @DisplayName("Convertit un match privé incomplet dans moins de 24h et crée une pénalité")
        void shouldConvertToPublicAndCreatePenalty_whenPrivateIncompleteWithin24Hours() {
            MembreEntity organisateur = membre(1);

            MatchEntity match = match(
                    10,
                    MatchType.PRIVE,
                    MatchStatus.PLANIFIE,
                    LocalDate.now().plusDays(1),
                    LocalTime.now().minusHours(1),
                    organisateur
            );

            match.setParticipations(List.of(
                    participation(1, organisateur, ParticipationStatut.PAYEE, match),
                    participation(2, membre(2), ParticipationStatut.PAYEE, match),
                    participation(3, membre(3), ParticipationStatut.EN_ATTENTE_PAIEMENT, match)
            ));

            when(matchRepository.findByTypeMatchAndStatut(MatchType.PRIVE, MatchStatus.PLANIFIE))
                    .thenReturn(List.of(match));

            when(penaliteRepository.existsByMembre_IdAndActiveTrueAndDateFinGreaterThanEqual(
                    eq(organisateur.getId()),
                    any(LocalDate.class)
            )).thenReturn(false);

            matchSchedulerService.convertirMatchsPrivesIncompletsEnPublics();

            assertEquals(MatchType.PUBLIC, match.getTypeMatch());
            assertEquals(MatchStatus.OUVERT, match.getStatut());

            verify(matchRepository).save(match);

            ArgumentCaptor<PenaliteEntity> penaliteCaptor =
                    ArgumentCaptor.forClass(PenaliteEntity.class);

            verify(penaliteRepository).save(penaliteCaptor.capture());

            PenaliteEntity penalite = penaliteCaptor.getValue();

            assertEquals(organisateur, penalite.getMembre());
            assertEquals(match, penalite.getMatch());
            assertTrue(penalite.isActive());
            assertEquals(LocalDate.now(), penalite.getDateDebut());
            assertEquals(LocalDate.now().plusDays(7), penalite.getDateFin());
            assertTrue(penalite.getRaison().contains("Match privé incomplet"));
        }

        @Test
        @DisplayName("Ne convertit pas un match privé complet")
        void shouldDoNothing_whenPrivateMatchIsComplete() {
            MembreEntity organisateur = membre(1);

            MatchEntity match = match(
                    11,
                    MatchType.PRIVE,
                    MatchStatus.PLANIFIE,
                    LocalDate.now().plusDays(1),
                    LocalTime.now().minusHours(1),
                    organisateur
            );

            match.setParticipations(List.of(
                    participation(1, organisateur, ParticipationStatut.PAYEE, match),
                    participation(2, membre(2), ParticipationStatut.PAYEE, match),
                    participation(3, membre(3), ParticipationStatut.PAYEE, match),
                    participation(4, membre(4), ParticipationStatut.EN_ATTENTE_PAIEMENT, match)
            ));

            when(matchRepository.findByTypeMatchAndStatut(MatchType.PRIVE, MatchStatus.PLANIFIE))
                    .thenReturn(List.of(match));

            matchSchedulerService.convertirMatchsPrivesIncompletsEnPublics();

            assertEquals(MatchType.PRIVE, match.getTypeMatch());
            assertEquals(MatchStatus.PLANIFIE, match.getStatut());

            verify(matchRepository, never()).save(any(MatchEntity.class));
            verify(penaliteRepository, never()).save(any(PenaliteEntity.class));
        }

        @Test
        @DisplayName("Ne convertit pas un match privé incomplet dans plus de 24h")
        void shouldDoNothing_whenMatchIsMoreThan24HoursAway() {
            MembreEntity organisateur = membre(1);

            MatchEntity match = match(
                    12,
                    MatchType.PRIVE,
                    MatchStatus.PLANIFIE,
                    LocalDate.now().plusDays(2),
                    LocalTime.NOON,
                    organisateur
            );

            match.setParticipations(List.of(
                    participation(1, organisateur, ParticipationStatut.PAYEE, match),
                    participation(2, membre(2), ParticipationStatut.PAYEE, match),
                    participation(3, membre(3), ParticipationStatut.PAYEE, match)
            ));

            when(matchRepository.findByTypeMatchAndStatut(MatchType.PRIVE, MatchStatus.PLANIFIE))
                    .thenReturn(List.of(match));

            matchSchedulerService.convertirMatchsPrivesIncompletsEnPublics();

            assertEquals(MatchType.PRIVE, match.getTypeMatch());
            assertEquals(MatchStatus.PLANIFIE, match.getStatut());

            verify(matchRepository, never()).save(any(MatchEntity.class));
            verify(penaliteRepository, never()).save(any(PenaliteEntity.class));
        }

        @Test
        @DisplayName("Une participation LIBEREE ne compte pas comme joueur actif")
        void shouldIgnoreFreedParticipation_whenCountingActivePlayers() {
            MembreEntity organisateur = membre(1);

            MatchEntity match = match(
                    13,
                    MatchType.PRIVE,
                    MatchStatus.PLANIFIE,
                    LocalDate.now(),
                    LocalTime.now().plusHours(1),
                    organisateur
            );

            match.setParticipations(List.of(
                    participation(1, organisateur, ParticipationStatut.PAYEE, match),
                    participation(2, membre(2), ParticipationStatut.PAYEE, match),
                    participation(3, membre(3), ParticipationStatut.PAYEE, match),
                    participation(4, membre(4), ParticipationStatut.LIBEREE, match)
            ));

            when(matchRepository.findByTypeMatchAndStatut(MatchType.PRIVE, MatchStatus.PLANIFIE))
                    .thenReturn(List.of(match));

            when(penaliteRepository.existsByMembre_IdAndActiveTrueAndDateFinGreaterThanEqual(
                    eq(organisateur.getId()),
                    any(LocalDate.class)
            )).thenReturn(false);

            matchSchedulerService.convertirMatchsPrivesIncompletsEnPublics();

            assertEquals(MatchType.PUBLIC, match.getTypeMatch());
            assertEquals(MatchStatus.OUVERT, match.getStatut());

            verify(matchRepository).save(match);
            verify(penaliteRepository).save(any(PenaliteEntity.class));
        }

        @Test
        @DisplayName("Convertit le match mais ne crée pas de pénalité si l'organisateur est absent")
        void shouldConvertToPublicWithoutPenalty_whenOrganizerIsNull() {
            MatchEntity match = match(
                    14,
                    MatchType.PRIVE,
                    MatchStatus.PLANIFIE,
                    LocalDate.now(),
                    LocalTime.now().plusHours(1),
                    null
            );

            match.setParticipations(List.of(
                    participation(1, membre(1), ParticipationStatut.PAYEE, match),
                    participation(2, membre(2), ParticipationStatut.PAYEE, match),
                    participation(3, membre(3), ParticipationStatut.PAYEE, match)
            ));

            when(matchRepository.findByTypeMatchAndStatut(MatchType.PRIVE, MatchStatus.PLANIFIE))
                    .thenReturn(List.of(match));

            matchSchedulerService.convertirMatchsPrivesIncompletsEnPublics();

            assertEquals(MatchType.PUBLIC, match.getTypeMatch());
            assertEquals(MatchStatus.OUVERT, match.getStatut());

            verify(matchRepository).save(match);
            verify(penaliteRepository, never()).save(any(PenaliteEntity.class));
        }

        @Test
        @DisplayName("Ne crée pas de deuxième pénalité si l'organisateur a déjà une pénalité active")
        void shouldNotCreatePenalty_whenOrganizerAlreadyHasActivePenalty() {
            MembreEntity organisateur = membre(1);

            MatchEntity match = match(
                    15,
                    MatchType.PRIVE,
                    MatchStatus.PLANIFIE,
                    LocalDate.now(),
                    LocalTime.now().plusHours(1),
                    organisateur
            );

            match.setParticipations(List.of(
                    participation(1, organisateur, ParticipationStatut.PAYEE, match),
                    participation(2, membre(2), ParticipationStatut.PAYEE, match),
                    participation(3, membre(3), ParticipationStatut.PAYEE, match)
            ));

            when(matchRepository.findByTypeMatchAndStatut(MatchType.PRIVE, MatchStatus.PLANIFIE))
                    .thenReturn(List.of(match));

            when(penaliteRepository.existsByMembre_IdAndActiveTrueAndDateFinGreaterThanEqual(
                    eq(organisateur.getId()),
                    any(LocalDate.class)
            )).thenReturn(true);

            matchSchedulerService.convertirMatchsPrivesIncompletsEnPublics();

            assertEquals(MatchType.PUBLIC, match.getTypeMatch());
            assertEquals(MatchStatus.OUVERT, match.getStatut());

            verify(matchRepository).save(match);
            verify(penaliteRepository, never()).save(any(PenaliteEntity.class));
        }

        @Test
        @DisplayName("Ignore un match sans date")
        void shouldIgnoreMatchWithoutDate() {
            MatchEntity match = new MatchEntity();
            match.setId(16);
            match.setTypeMatch(MatchType.PRIVE);
            match.setStatut(MatchStatus.PLANIFIE);
            match.setHeureDebut(LocalTime.now());
            match.setOrganisateur(membre(1));

            when(matchRepository.findByTypeMatchAndStatut(MatchType.PRIVE, MatchStatus.PLANIFIE))
                    .thenReturn(List.of(match));

            matchSchedulerService.convertirMatchsPrivesIncompletsEnPublics();

            verify(matchRepository, never()).save(any(MatchEntity.class));
            verify(penaliteRepository, never()).save(any(PenaliteEntity.class));
        }

        @Test
        @DisplayName("Ignore un match sans heure de début")
        void shouldIgnoreMatchWithoutStartTime() {
            MatchEntity match = new MatchEntity();
            match.setId(17);
            match.setTypeMatch(MatchType.PRIVE);
            match.setStatut(MatchStatus.PLANIFIE);
            match.setDateMatch(LocalDate.now());
            match.setOrganisateur(membre(1));

            when(matchRepository.findByTypeMatchAndStatut(MatchType.PRIVE, MatchStatus.PLANIFIE))
                    .thenReturn(List.of(match));

            matchSchedulerService.convertirMatchsPrivesIncompletsEnPublics();

            verify(matchRepository, never()).save(any(MatchEntity.class));
            verify(penaliteRepository, never()).save(any(PenaliteEntity.class));
        }
    }

    @Nested
    @DisplayName("Libération des places non payées")
    class LibererPlacesNonPayees {

        @Test
        @DisplayName("Libère une participation impayée, crée une dette et rend le match public")
        void shouldCreateDebtFreeParticipationAndMakeMatchPublic_whenNonOrganizerDidNotPay() {
            MembreEntity organisateur = membre(1);
            MembreEntity joueur = membre(2);

            MatchEntity match = match(
                    20,
                    MatchType.PRIVE,
                    MatchStatus.PLANIFIE,
                    LocalDate.now(),
                    LocalTime.now().plusHours(2),
                    organisateur
            );

            ReservationEntity reservation = reservation(100);
            match.setReservation(reservation);

            ParticipationEntity participation =
                    participation(10, joueur, ParticipationStatut.EN_ATTENTE_PAIEMENT, match);
            participation.setMontantDuCentimes(1500);

            when(participationRepository.findByStatutAndDateLimitePaiementBefore(
                    eq(ParticipationStatut.EN_ATTENTE_PAIEMENT),
                    any(LocalDateTime.class)
            )).thenReturn(List.of(participation));

            matchSchedulerService.libererPlacesNonPayees();

            assertEquals(ParticipationStatut.LIBEREE, participation.getStatut());
            assertEquals(MatchType.PUBLIC, match.getTypeMatch());
            assertEquals(MatchStatus.OUVERT, match.getStatut());

            verify(paiementService).createDebt(
                    joueur,
                    participation,
                    reservation,
                    1500,
                    DetteRaison.PARTICIPATION_IMPAYEE
            );

            verify(participationRepository).save(participation);
            verify(matchRepository).save(match);
            verify(penaliteRepository, never()).save(any(PenaliteEntity.class));
        }

        @Test
        @DisplayName("Crée aussi une pénalité si la participation impayée est celle de l'organisateur")
        void shouldCreatePenalty_whenOrganizerDidNotPay() {
            MembreEntity organisateur = membre(1);

            MatchEntity match = match(
                    21,
                    MatchType.PRIVE,
                    MatchStatus.PLANIFIE,
                    LocalDate.now(),
                    LocalTime.now().plusHours(2),
                    organisateur
            );

            ReservationEntity reservation = reservation(101);
            match.setReservation(reservation);

            ParticipationEntity participation =
                    participation(11, organisateur, ParticipationStatut.EN_ATTENTE_PAIEMENT, match);
            participation.setMontantDuCentimes(1500);

            when(participationRepository.findByStatutAndDateLimitePaiementBefore(
                    eq(ParticipationStatut.EN_ATTENTE_PAIEMENT),
                    any(LocalDateTime.class)
            )).thenReturn(List.of(participation));

            when(penaliteRepository.existsByMembre_IdAndActiveTrueAndDateFinGreaterThanEqual(
                    eq(organisateur.getId()),
                    any(LocalDate.class)
            )).thenReturn(false);

            matchSchedulerService.libererPlacesNonPayees();

            assertEquals(ParticipationStatut.LIBEREE, participation.getStatut());
            assertEquals(MatchType.PUBLIC, match.getTypeMatch());
            assertEquals(MatchStatus.OUVERT, match.getStatut());

            verify(paiementService).createDebt(
                    organisateur,
                    participation,
                    reservation,
                    1500,
                    DetteRaison.PARTICIPATION_IMPAYEE
            );

            verify(participationRepository).save(participation);
            verify(matchRepository).save(match);
            verify(penaliteRepository).save(any(PenaliteEntity.class));
        }

        @Test
        @DisplayName("Ignore une participation en retard sans match")
        void shouldIgnoreParticipation_whenMatchIsNull() {
            ParticipationEntity participation = new ParticipationEntity();
            participation.setId(12);
            participation.setMatch(null);
            participation.setMembre(membre(2));
            participation.setStatut(ParticipationStatut.EN_ATTENTE_PAIEMENT);

            when(participationRepository.findByStatutAndDateLimitePaiementBefore(
                    eq(ParticipationStatut.EN_ATTENTE_PAIEMENT),
                    any(LocalDateTime.class)
            )).thenReturn(List.of(participation));

            matchSchedulerService.libererPlacesNonPayees();

            verify(paiementService, never()).createDebt(any(), any(), any(), anyInt(), any());
            verify(participationRepository, never()).save(any(ParticipationEntity.class));
            verify(matchRepository, never()).save(any(MatchEntity.class));
            verify(penaliteRepository, never()).save(any(PenaliteEntity.class));
        }

        @Test
        @DisplayName("Traite toutes les participations en retard")
        void shouldProcessAllOverdueParticipations() {
            MembreEntity organisateur1 = membre(1);
            MembreEntity joueur1 = membre(2);

            MembreEntity organisateur2 = membre(3);
            MembreEntity joueur2 = membre(4);

            MatchEntity match1 = match(
                    24,
                    MatchType.PRIVE,
                    MatchStatus.PLANIFIE,
                    LocalDate.now(),
                    LocalTime.now().plusHours(2),
                    organisateur1
            );

            MatchEntity match2 = match(
                    25,
                    MatchType.PRIVE,
                    MatchStatus.PLANIFIE,
                    LocalDate.now(),
                    LocalTime.now().plusHours(3),
                    organisateur2
            );

            ParticipationEntity participation1 =
                    participation(15, joueur1, ParticipationStatut.EN_ATTENTE_PAIEMENT, match1);
            participation1.setMontantDuCentimes(1500);

            ParticipationEntity participation2 =
                    participation(16, joueur2, ParticipationStatut.EN_ATTENTE_PAIEMENT, match2);
            participation2.setMontantDuCentimes(1500);

            when(participationRepository.findByStatutAndDateLimitePaiementBefore(
                    eq(ParticipationStatut.EN_ATTENTE_PAIEMENT),
                    any(LocalDateTime.class)
            )).thenReturn(List.of(participation1, participation2));

            matchSchedulerService.libererPlacesNonPayees();

            assertEquals(ParticipationStatut.LIBEREE, participation1.getStatut());
            assertEquals(ParticipationStatut.LIBEREE, participation2.getStatut());

            verify(paiementService, times(2))
                    .createDebt(any(), any(), any(), anyInt(), eq(DetteRaison.PARTICIPATION_IMPAYEE));

            verify(participationRepository, times(2)).save(any(ParticipationEntity.class));
            verify(matchRepository, times(2)).save(any(MatchEntity.class));
        }

        @Test
        @DisplayName("Ne fait rien si aucune participation en retard n'existe")
        void shouldDoNothing_whenNoOverdueParticipationExists() {
            when(participationRepository.findByStatutAndDateLimitePaiementBefore(
                    eq(ParticipationStatut.EN_ATTENTE_PAIEMENT),
                    any(LocalDateTime.class)
            )).thenReturn(List.of());

            matchSchedulerService.libererPlacesNonPayees();

            verify(paiementService, never()).createDebt(any(), any(), any(), anyInt(), any());
            verify(participationRepository, never()).save(any(ParticipationEntity.class));
            verify(matchRepository, never()).save(any(MatchEntity.class));
            verify(penaliteRepository, never()).save(any(PenaliteEntity.class));
        }
    }

    @Nested
    @DisplayName("Facturation du solde organisateur des matchs publics incomplets")
    class FacturerSoldesOrganisateursMatchsPublicsIncomplets {

        @Test
        @DisplayName("Facture les matchs publics PLANIFIE, OUVERT et COMPLET dont l'heure est passée")
        void shouldBillStartedPublicMatches() {
            MatchEntity matchPlanifiePasse = match(
                    30,
                    MatchType.PUBLIC,
                    MatchStatus.PLANIFIE,
                    LocalDate.now().minusDays(1),
                    LocalTime.NOON,
                    membre(1)
            );

            MatchEntity matchOuvertPasse = match(
                    31,
                    MatchType.PUBLIC,
                    MatchStatus.OUVERT,
                    LocalDate.now(),
                    LocalTime.now().minusHours(1),
                    membre(2)
            );

            MatchEntity matchCompletPasse = match(
                    32,
                    MatchType.PUBLIC,
                    MatchStatus.COMPLET,
                    LocalDate.now().minusDays(2),
                    LocalTime.NOON,
                    membre(3)
            );

            when(matchRepository.findByTypeMatchAndStatut(MatchType.PUBLIC, MatchStatus.PLANIFIE))
                    .thenReturn(List.of(matchPlanifiePasse));

            when(matchRepository.findByTypeMatchAndStatut(MatchType.PUBLIC, MatchStatus.OUVERT))
                    .thenReturn(List.of(matchOuvertPasse));

            when(matchRepository.findByTypeMatchAndStatut(MatchType.PUBLIC, MatchStatus.COMPLET))
                    .thenReturn(List.of(matchCompletPasse));

            matchSchedulerService.facturerSoldesOrganisateursMatchsPublicsIncomplets();

            verify(matchBillingService).facturerSoldeOrganisateurSiNecessaire(matchPlanifiePasse);
            verify(matchBillingService).facturerSoldeOrganisateurSiNecessaire(matchOuvertPasse);
            verify(matchBillingService).facturerSoldeOrganisateurSiNecessaire(matchCompletPasse);
        }

        @Test
        @DisplayName("Ne facture pas un match public futur")
        void shouldNotBillFuturePublicMatch() {
            MatchEntity matchFutur = match(
                    33,
                    MatchType.PUBLIC,
                    MatchStatus.OUVERT,
                    LocalDate.now().plusDays(1),
                    LocalTime.NOON,
                    membre(1)
            );

            when(matchRepository.findByTypeMatchAndStatut(MatchType.PUBLIC, MatchStatus.PLANIFIE))
                    .thenReturn(List.of());

            when(matchRepository.findByTypeMatchAndStatut(MatchType.PUBLIC, MatchStatus.OUVERT))
                    .thenReturn(List.of(matchFutur));

            when(matchRepository.findByTypeMatchAndStatut(MatchType.PUBLIC, MatchStatus.COMPLET))
                    .thenReturn(List.of());

            matchSchedulerService.facturerSoldesOrganisateursMatchsPublicsIncomplets();

            verify(matchBillingService, never()).facturerSoldeOrganisateurSiNecessaire(any(MatchEntity.class));
        }

        @Test
        @DisplayName("Ignore un match public sans date")
        void shouldIgnorePublicMatchWithoutDate() {
            MatchEntity matchSansDate = new MatchEntity();
            matchSansDate.setId(34);
            matchSansDate.setTypeMatch(MatchType.PUBLIC);
            matchSansDate.setStatut(MatchStatus.OUVERT);
            matchSansDate.setHeureDebut(LocalTime.NOON);

            when(matchRepository.findByTypeMatchAndStatut(MatchType.PUBLIC, MatchStatus.PLANIFIE))
                    .thenReturn(List.of());

            when(matchRepository.findByTypeMatchAndStatut(MatchType.PUBLIC, MatchStatus.OUVERT))
                    .thenReturn(List.of(matchSansDate));

            when(matchRepository.findByTypeMatchAndStatut(MatchType.PUBLIC, MatchStatus.COMPLET))
                    .thenReturn(List.of());

            matchSchedulerService.facturerSoldesOrganisateursMatchsPublicsIncomplets();

            verify(matchBillingService, never()).facturerSoldeOrganisateurSiNecessaire(any(MatchEntity.class));
        }

        @Test
        @DisplayName("Ignore un match public sans heure de début")
        void shouldIgnorePublicMatchWithoutStartTime() {
            MatchEntity matchSansHeure = new MatchEntity();
            matchSansHeure.setId(35);
            matchSansHeure.setTypeMatch(MatchType.PUBLIC);
            matchSansHeure.setStatut(MatchStatus.OUVERT);
            matchSansHeure.setDateMatch(LocalDate.now());

            when(matchRepository.findByTypeMatchAndStatut(MatchType.PUBLIC, MatchStatus.PLANIFIE))
                    .thenReturn(List.of());

            when(matchRepository.findByTypeMatchAndStatut(MatchType.PUBLIC, MatchStatus.OUVERT))
                    .thenReturn(List.of(matchSansHeure));

            when(matchRepository.findByTypeMatchAndStatut(MatchType.PUBLIC, MatchStatus.COMPLET))
                    .thenReturn(List.of());

            matchSchedulerService.facturerSoldesOrganisateursMatchsPublicsIncomplets();

            verify(matchBillingService, never()).facturerSoldeOrganisateurSiNecessaire(any(MatchEntity.class));
        }

        @Test
        @DisplayName("Ne fait rien si aucun match public facturable n'existe")
        void shouldDoNothing_whenNoPublicMatchExists() {
            when(matchRepository.findByTypeMatchAndStatut(MatchType.PUBLIC, MatchStatus.PLANIFIE))
                    .thenReturn(List.of());

            when(matchRepository.findByTypeMatchAndStatut(MatchType.PUBLIC, MatchStatus.OUVERT))
                    .thenReturn(List.of());

            when(matchRepository.findByTypeMatchAndStatut(MatchType.PUBLIC, MatchStatus.COMPLET))
                    .thenReturn(List.of());

            matchSchedulerService.facturerSoldesOrganisateursMatchsPublicsIncomplets();

            verify(matchBillingService, never()).facturerSoldeOrganisateurSiNecessaire(any(MatchEntity.class));
        }
    }

    @Nested
    @DisplayName("Désactivation des pénalités expirées")
    class DesactiverPenalitesExpirees {

        @Test
        @DisplayName("Désactive une pénalité expirée")
        void shouldSetActiveFalse_whenPenaltyExpired() {
            PenaliteEntity penalite = new PenaliteEntity();
            penalite.setId(1);
            penalite.setActive(true);
            penalite.setDateFin(LocalDate.now());

            when(penaliteRepository.findByActiveTrueAndDateFinLessThanEqual(any(LocalDate.class)))
                    .thenReturn(List.of(penalite));

            matchSchedulerService.desactiverPenalitesExpirees();

            assertFalse(penalite.isActive());
            verify(penaliteRepository).save(penalite);
        }

        @Test
        @DisplayName("Désactive toutes les pénalités expirées")
        void shouldDeactivateAllExpiredPenalties() {
            PenaliteEntity penalite1 = new PenaliteEntity();
            penalite1.setId(1);
            penalite1.setActive(true);
            penalite1.setDateFin(LocalDate.now());

            PenaliteEntity penalite2 = new PenaliteEntity();
            penalite2.setId(2);
            penalite2.setActive(true);
            penalite2.setDateFin(LocalDate.now().minusDays(1));

            when(penaliteRepository.findByActiveTrueAndDateFinLessThanEqual(any(LocalDate.class)))
                    .thenReturn(List.of(penalite1, penalite2));

            matchSchedulerService.desactiverPenalitesExpirees();

            assertFalse(penalite1.isActive());
            assertFalse(penalite2.isActive());

            verify(penaliteRepository).save(penalite1);
            verify(penaliteRepository).save(penalite2);
        }

        @Test
        @DisplayName("Ne fait rien si aucune pénalité n'est expirée")
        void shouldDoNothing_whenNoExpiredPenalty() {
            when(penaliteRepository.findByActiveTrueAndDateFinLessThanEqual(any(LocalDate.class)))
                    .thenReturn(List.of());

            matchSchedulerService.desactiverPenalitesExpirees();

            verify(penaliteRepository, never()).save(any(PenaliteEntity.class));
        }
    }

    private MatchEntity match(
            Integer id,
            MatchType type,
            MatchStatus status,
            LocalDate date,
            LocalTime heureDebut,
            MembreEntity organisateur
    ) {
        MatchEntity match = new MatchEntity();
        match.setId(id);
        match.setTypeMatch(type);
        match.setStatut(status);
        match.setDateMatch(date);
        match.setHeureDebut(heureDebut);
        match.setOrganisateur(organisateur);
        return match;
    }

    private ParticipationEntity participation(
            Integer id,
            MembreEntity membre,
            ParticipationStatut statut,
            MatchEntity match
    ) {
        ParticipationEntity participation = new ParticipationEntity();
        participation.setId(id);
        participation.setMembre(membre);
        participation.setStatut(statut);
        participation.setMatch(match);
        participation.setMontantDuCentimes(1500);
        return participation;
    }

    private MembreEntity membre(Integer id) {
        MembreEntity membre = new MembreEntity();
        membre.setId(id);
        membre.setActif(true);
        membre.setMatricule("G000" + id);
        return membre;
    }

    private ReservationEntity reservation(Integer id) {
        ReservationEntity reservation = new ReservationEntity();
        reservation.setId(id);
        return reservation;
    }
}