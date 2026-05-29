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
import org.junit.jupiter.api.BeforeEach;
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

    private MatchSchedulerService matchSchedulerService;

    @BeforeEach
    void setUp() {
        matchSchedulerService = new MatchSchedulerService(
                matchRepository,
                participationRepository,
                penaliteRepository,
                paiementService
        );
    }

    /**
     * Scénario 1 :
     * Match privé incomplet dans moins de 24h
     * => passe public + pénalité organisateur.
     */
    @Test
    void convertirMatchsPrivesIncompletsEnPublics_shouldConvertToPublicAndCreatePenalty_whenPrivateIncompleteWithin24Hours() {
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
        verify(penaliteRepository).save(any(PenaliteEntity.class));
    }

    /**
     * Scénario 2 :
     * Match privé complet dans moins de 24h
     * => rien ne change.
     */
    @Test
    void convertirMatchsPrivesIncompletsEnPublics_shouldDoNothing_whenPrivateMatchIsComplete() {
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

    /**
     * Scénario 3 :
     * Match privé incomplet mais dans plus de 24h
     * => ne passe pas encore public.
     */
    @Test
    void convertirMatchsPrivesIncompletsEnPublics_shouldDoNothing_whenMatchIsMoreThan24HoursAway() {
        MembreEntity organisateur = membre(1);

        MatchEntity match = match(
                12,
                MatchType.PRIVE,
                MatchStatus.PLANIFIE,
                LocalDate.now().plusDays(2),
                LocalTime.now(),
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

    /**
     * Scénario 4 :
     * Match privé incomplet mais statut non modifiable
     * => ne passe pas public.
     */
    @Test
    void convertirMatchsPrivesIncompletsEnPublics_shouldDoNothing_whenMatchCannotBeMadePublic() {
        MembreEntity organisateur = membre(1);

        MatchEntity match = match(
                13,
                MatchType.PRIVE,
                MatchStatus.ANNULE,
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

        matchSchedulerService.convertirMatchsPrivesIncompletsEnPublics();

        assertEquals(MatchType.PRIVE, match.getTypeMatch());
        assertEquals(MatchStatus.ANNULE, match.getStatut());

        verify(matchRepository, never()).save(any(MatchEntity.class));
        verify(penaliteRepository, never()).save(any(PenaliteEntity.class));
    }

    /**
     * Scénario 5 :
     * Participation LIBEREE non comptée comme active
     * => le match devient incomplet et passe public.
     */
    @Test
    void convertirMatchsPrivesIncompletsEnPublics_shouldIgnoreFreedParticipation_whenCountingActivePlayers() {
        MembreEntity organisateur = membre(1);

        MatchEntity match = match(
                14,
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

    /**
     * Scénario 6 :
     * Organisateur absent
     * => le match passe public mais aucune pénalité n'est créée.
     */
    @Test
    void convertirMatchsPrivesIncompletsEnPublics_shouldConvertToPublicWithoutPenalty_whenOrganizerIsNull() {
        MatchEntity match = match(
                15,
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

    /**
     * Scénario 7 :
     * Pénalité déjà active pour l'organisateur
     * => pas de nouvelle pénalité.
     */
    @Test
    void convertirMatchsPrivesIncompletsEnPublics_shouldNotCreatePenalty_whenOrganizerAlreadyHasActivePenalty() {
        MembreEntity organisateur = membre(1);

        MatchEntity match = match(
                16,
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

    /**
     * Scénario 8 :
     * Participant non organisateur impayé
     * => dette + participation LIBEREE + match public.
     */
    @Test
    void libererPlacesNonPayees_shouldCreateDebtFreeParticipationAndMakeMatchPublic_whenNonOrganizerDidNotPay() {
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

        ReservationEntity reservation = new ReservationEntity();
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
                eq(joueur),
                eq(participation),
                eq(reservation),
                eq(1500),
                eq(DetteRaison.PARTICIPATION_IMPAYEE)
        );

        verify(participationRepository).save(participation);
        verify(matchRepository).save(match);
        verify(penaliteRepository, never()).save(any(PenaliteEntity.class));
    }

    /**
     * Scénario 9 :
     * Organisateur impayé
     * => dette + participation LIBEREE + match public + pénalité.
     */
    @Test
    void libererPlacesNonPayees_shouldCreateDebtFreeParticipationMakePublicAndCreatePenalty_whenOrganizerDidNotPay() {
        MembreEntity organisateur = membre(1);

        MatchEntity match = match(
                21,
                MatchType.PRIVE,
                MatchStatus.PLANIFIE,
                LocalDate.now(),
                LocalTime.now().plusHours(2),
                organisateur
        );

        ReservationEntity reservation = new ReservationEntity();
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
                eq(organisateur),
                eq(participation),
                eq(reservation),
                eq(1500),
                eq(DetteRaison.PARTICIPATION_IMPAYEE)
        );

        verify(participationRepository).save(participation);
        verify(matchRepository).save(match);
        verify(penaliteRepository).save(any(PenaliteEntity.class));
    }

    /**
     * Scénario 10 :
     * Participation en retard sans match
     * => aucune action.
     */
    @Test
    void libererPlacesNonPayees_shouldIgnoreParticipation_whenMatchIsNull() {
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

    /**
     * Scénario 11 :
     * Participation impayée sur match non modifiable
     * => dette + participation LIBEREE, mais pas de passage public.
     */
    @Test
    void libererPlacesNonPayees_shouldNotMakeMatchPublic_whenMatchCannotBeMadePublic() {
        MembreEntity organisateur = membre(1);
        MembreEntity joueur = membre(2);

        MatchEntity match = match(
                22,
                MatchType.PRIVE,
                MatchStatus.ANNULE,
                LocalDate.now(),
                LocalTime.now().plusHours(2),
                organisateur
        );

        ReservationEntity reservation = new ReservationEntity();
        match.setReservation(reservation);

        ParticipationEntity participation =
                participation(13, joueur, ParticipationStatut.EN_ATTENTE_PAIEMENT, match);
        participation.setMontantDuCentimes(1500);

        when(participationRepository.findByStatutAndDateLimitePaiementBefore(
                eq(ParticipationStatut.EN_ATTENTE_PAIEMENT),
                any(LocalDateTime.class)
        )).thenReturn(List.of(participation));

        matchSchedulerService.libererPlacesNonPayees();

        assertEquals(ParticipationStatut.LIBEREE, participation.getStatut());
        assertEquals(MatchType.PRIVE, match.getTypeMatch());
        assertEquals(MatchStatus.ANNULE, match.getStatut());

        verify(paiementService).createDebt(
                eq(joueur),
                eq(participation),
                eq(reservation),
                eq(1500),
                eq(DetteRaison.PARTICIPATION_IMPAYEE)
        );

        verify(participationRepository).save(participation);
        verify(matchRepository, never()).save(any(MatchEntity.class));
    }

    /**
     * Scénario 12 :
     * Vérifie que la dette est créée avec les bonnes données.
     */
    @Test
    void libererPlacesNonPayees_shouldCreateDebtWithCorrectArguments() {
        MembreEntity organisateur = membre(1);
        MembreEntity joueur = membre(2);

        ReservationEntity reservation = new ReservationEntity();

        MatchEntity match = match(
                23,
                MatchType.PUBLIC,
                MatchStatus.OUVERT,
                LocalDate.now(),
                LocalTime.now().plusHours(2),
                organisateur
        );
        match.setReservation(reservation);

        ParticipationEntity participation =
                participation(14, joueur, ParticipationStatut.EN_ATTENTE_PAIEMENT, match);
        participation.setMontantDuCentimes(1500);

        when(participationRepository.findByStatutAndDateLimitePaiementBefore(
                eq(ParticipationStatut.EN_ATTENTE_PAIEMENT),
                any(LocalDateTime.class)
        )).thenReturn(List.of(participation));

        matchSchedulerService.libererPlacesNonPayees();

        verify(paiementService).createDebt(
                joueur,
                participation,
                reservation,
                1500,
                DetteRaison.PARTICIPATION_IMPAYEE
        );
    }

    /**
     * Scénario 13 :
     * Plusieurs participations en retard
     * => toutes sont traitées.
     */
    @Test
    void libererPlacesNonPayees_shouldProcessAllOverdueParticipations() {
        MembreEntity organisateur1 = membre(1);
        MembreEntity joueur1 = membre(2);

        MembreEntity organisateur2 = membre(3);
        MembreEntity joueur2 = membre(4);

        MatchEntity match1 = match(24, MatchType.PRIVE, MatchStatus.PLANIFIE, LocalDate.now(), LocalTime.now().plusHours(2), organisateur1);
        MatchEntity match2 = match(25, MatchType.PRIVE, MatchStatus.PLANIFIE, LocalDate.now(), LocalTime.now().plusHours(3), organisateur2);

        ParticipationEntity participation1 = participation(15, joueur1, ParticipationStatut.EN_ATTENTE_PAIEMENT, match1);
        participation1.setMontantDuCentimes(1500);

        ParticipationEntity participation2 = participation(16, joueur2, ParticipationStatut.EN_ATTENTE_PAIEMENT, match2);
        participation2.setMontantDuCentimes(1500);

        when(participationRepository.findByStatutAndDateLimitePaiementBefore(
                eq(ParticipationStatut.EN_ATTENTE_PAIEMENT),
                any(LocalDateTime.class)
        )).thenReturn(List.of(participation1, participation2));

        matchSchedulerService.libererPlacesNonPayees();

        assertEquals(ParticipationStatut.LIBEREE, participation1.getStatut());
        assertEquals(ParticipationStatut.LIBEREE, participation2.getStatut());

        verify(paiementService, times(2)).createDebt(any(), any(), any(), anyInt(), eq(DetteRaison.PARTICIPATION_IMPAYEE));
        verify(participationRepository, times(2)).save(any(ParticipationEntity.class));
        verify(matchRepository, times(2)).save(any(MatchEntity.class));
    }

    /**
     * Scénario 14 :
     * Pénalité expirée
     * => active=false.
     */
    @Test
    void desactiverPenalitesExpirees_shouldSetActiveFalse_whenPenaltyExpired() {
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

    /**
     * Scénario 15 :
     * Aucune pénalité expirée
     * => rien à sauvegarder.
     */
    @Test
    void desactiverPenalitesExpirees_shouldDoNothing_whenNoExpiredPenalty() {
        when(penaliteRepository.findByActiveTrueAndDateFinLessThanEqual(any(LocalDate.class)))
                .thenReturn(List.of());

        matchSchedulerService.desactiverPenalitesExpirees();

        verify(penaliteRepository, never()).save(any(PenaliteEntity.class));
    }

    /**
     * Scénario 16 :
     * Plusieurs pénalités expirées
     * => toutes passent inactive.
     */
    @Test
    void desactiverPenalitesExpirees_shouldDeactivateAllExpiredPenalties() {
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

        verify(penaliteRepository, times(2)).save(any(PenaliteEntity.class));
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
}