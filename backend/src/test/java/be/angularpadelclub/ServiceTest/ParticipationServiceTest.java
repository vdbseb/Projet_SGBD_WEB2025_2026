package be.angularpadelclub.ServiceTest;

import be.angularpadelclub.Entity.MatchEntity;
import be.angularpadelclub.Entity.MembreEntity;
import be.angularpadelclub.Entity.ParticipationEntity;
import be.angularpadelclub.Enum.ParticipationStatut;
import be.angularpadelclub.Repository.ParticipationRepository;
import be.angularpadelclub.Service.ClubBusinessRules;
import be.angularpadelclub.Service.ParticipationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ParticipationServiceTest {

    private ParticipationRepository participationRepository;
    private ParticipationService service;

    @BeforeEach
    void setUp() {
        participationRepository = mock(ParticipationRepository.class);
        service = new ParticipationService(participationRepository);
    }

    @Test
    void createPendingParticipation_createsExpectedParticipation() {
        MatchEntity match = new MatchEntity();
        match.setDateMatch(LocalDate.of(2026, 7, 10));
        match.setHeureDebut(LocalTime.of(10, 0));

        MembreEntity membre = new MembreEntity();

        when(participationRepository.save(any(ParticipationEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ParticipationEntity result = service.createPendingParticipation(match, membre);

        assertSame(match, result.getMatch());
        assertSame(membre, result.getMembre());
        assertEquals(ParticipationStatut.EN_ATTENTE_PAIEMENT, result.getStatut());
        assertEquals(
                ClubBusinessRules.DEFAULT_PLAYER_SHARE_CENTS,
                result.getMontantDuCentimes()
        );
        assertEquals(
                LocalDate.of(2026, 7, 9).atTime(10, 0),
                result.getDateLimitePaiement()
        );

        verify(participationRepository).save(any(ParticipationEntity.class));
    }

    @Test
    void createPendingParticipation_rejectsNullMatch() {
        MembreEntity membre = new MembreEntity();

        assertThrows(
                ResponseStatusException.class,
                () -> service.createPendingParticipation(null, membre)
        );
    }

    @Test
    void createPendingParticipation_rejectsNullMember() {
        MatchEntity match = new MatchEntity();
        match.setDateMatch(LocalDate.of(2026, 7, 10));
        match.setHeureDebut(LocalTime.of(10, 0));

        assertThrows(
                ResponseStatusException.class,
                () -> service.createPendingParticipation(match, null)
        );
    }

    @Test
    void createPendingParticipation_rejectsMatchWithoutDateOrStartTime() {
        MatchEntity match = new MatchEntity();
        MembreEntity membre = new MembreEntity();

        assertThrows(
                ResponseStatusException.class,
                () -> service.createPendingParticipation(match, membre)
        );
    }

    @Test
    void isActiveParticipation_returnsTrueForPendingAndPaid() {
        assertTrue(service.isActiveParticipation(ParticipationStatut.EN_ATTENTE_PAIEMENT));
        assertTrue(service.isActiveParticipation(ParticipationStatut.PAYEE));
    }

    @Test
    void isActiveParticipation_returnsFalseForOtherStatuses() {
        assertFalse(service.isActiveParticipation(ParticipationStatut.LIBEREE));
        assertFalse(service.isActiveParticipation(ParticipationStatut.ANNULEE));
    }
}