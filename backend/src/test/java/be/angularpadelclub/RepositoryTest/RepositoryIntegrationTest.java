package be.angularpadelclub.RepositoryTest;

import be.angularpadelclub.Repository.AdministrateurRepository;
import be.angularpadelclub.Repository.CourtRepository;
import be.angularpadelclub.Repository.DetteMembreRepository;
import be.angularpadelclub.Repository.HoraireSiteRepository;
import be.angularpadelclub.Repository.JourFermetureRepository;
import be.angularpadelclub.Repository.MatchRepository;
import be.angularpadelclub.Repository.MembreRepository;
import be.angularpadelclub.Repository.PaiementRepository;
import be.angularpadelclub.Repository.ParticipationRepository;
import be.angularpadelclub.Repository.PenaliteRepository;
import be.angularpadelclub.Repository.ReservationRepository;
import be.angularpadelclub.Repository.SiteRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class RepositoryIntegrationTest {

    @Test
    void repositoryContracts_canBeMockedForServiceTests() {
        SiteRepository siteRepository = mock(SiteRepository.class);
        CourtRepository courtRepository = mock(CourtRepository.class);
        MembreRepository membreRepository = mock(MembreRepository.class);
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        MatchRepository matchRepository = mock(MatchRepository.class);
        ParticipationRepository participationRepository = mock(ParticipationRepository.class);
        PaiementRepository paiementRepository = mock(PaiementRepository.class);
        DetteMembreRepository detteMembreRepository = mock(DetteMembreRepository.class);
        PenaliteRepository penaliteRepository = mock(PenaliteRepository.class);
        HoraireSiteRepository horaireSiteRepository = mock(HoraireSiteRepository.class);
        JourFermetureRepository jourFermetureRepository = mock(JourFermetureRepository.class);
        AdministrateurRepository administrateurRepository = mock(AdministrateurRepository.class);

        assertNotNull(siteRepository);
        assertNotNull(courtRepository);
        assertNotNull(membreRepository);
        assertNotNull(reservationRepository);
        assertNotNull(matchRepository);
        assertNotNull(participationRepository);
        assertNotNull(paiementRepository);
        assertNotNull(detteMembreRepository);
        assertNotNull(penaliteRepository);
        assertNotNull(horaireSiteRepository);
        assertNotNull(jourFermetureRepository);
        assertNotNull(administrateurRepository);
    }
}