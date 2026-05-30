package be.angularpadelclub.ServiceTest;

import be.angularpadelclub.DTO.ReservationDTO;
import be.angularpadelclub.Entity.*;
import be.angularpadelclub.Enum.MatchStatus;
import be.angularpadelclub.Enum.MatchType;
import be.angularpadelclub.Enum.ParticipationStatut;
import be.angularpadelclub.Enum.ReservationStatus;
import be.angularpadelclub.Mapper.ReservationMapper;
import be.angularpadelclub.Repository.CourtRepository;
import be.angularpadelclub.Repository.MatchRepository;
import be.angularpadelclub.Repository.MembreRepository;
import be.angularpadelclub.Repository.ParticipationRepository;
import be.angularpadelclub.Repository.ReservationRepository;
import be.angularpadelclub.Service.ReservationCancellationService;
import be.angularpadelclub.Service.ReservationService;
import be.angularpadelclub.Service.ReservationValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private CourtRepository courtRepository;

    @Mock
    private MembreRepository membreRepository;

    @Mock
    private ReservationMapper reservationMapper;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private ParticipationRepository participationRepository;

    @Mock
    private ReservationValidationService reservationValidationService;

    @Mock
    private ReservationCancellationService reservationCancellationService;

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(
                reservationRepository,
                courtRepository,
                membreRepository,
                reservationMapper,
                matchRepository,
                participationRepository,
                reservationValidationService,
                reservationCancellationService
        );
    }

    @Test
    @DisplayName("findAll retourne les réservations mappées")
    void findAll_shouldReturnMappedReservations() {
        ReservationEntity reservation1 = reservation(1);
        ReservationEntity reservation2 = reservation(2);

        ReservationDTO dto1 = dto(1, null, List.of());
        ReservationDTO dto2 = dto(2, null, List.of());

        when(reservationRepository.findAll()).thenReturn(List.of(reservation1, reservation2));
        when(reservationMapper.toDTO(reservation1)).thenReturn(dto1);
        when(reservationMapper.toDTO(reservation2)).thenReturn(dto2);

        List<ReservationDTO> result = reservationService.findAll();

        assertEquals(2, result.size());
        assertEquals(dto1, result.get(0));
        assertEquals(dto2, result.get(1));
    }

    @Test
    @DisplayName("findById retourne un DTO si la réservation existe")
    void findById_shouldReturnDtoWhenReservationExists() {
        ReservationEntity reservation = reservation(1);
        ReservationDTO dto = dto(1, null, List.of());

        when(reservationRepository.findById(1)).thenReturn(Optional.of(reservation));
        when(reservationMapper.toDTO(reservation)).thenReturn(dto);

        Optional<ReservationDTO> result = reservationService.findById(1);

        assertTrue(result.isPresent());
        assertEquals(dto, result.get());
    }

    @Test
    @DisplayName("findById retourne Optional.empty si la réservation n'existe pas")
    void findById_shouldReturnEmptyWhenReservationDoesNotExist() {
        when(reservationRepository.findById(999)).thenReturn(Optional.empty());

        Optional<ReservationDTO> result = reservationService.findById(999);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findByCourtAndDate délègue au repository")
    void findByCourtAndDate_shouldDelegateToRepository() {
        LocalDate date = LocalDate.of(2026, 6, 1);
        ReservationEntity reservation = reservation(1);

        when(reservationRepository.findByCourtIdAndDate(1, date))
                .thenReturn(List.of(reservation));

        List<ReservationEntity> result = reservationService.findByCourtAndDate(1, date);

        assertEquals(List.of(reservation), result);
    }

    @Test
    @DisplayName("deleteReservation délègue au repository")
    void deleteReservation_shouldDelegateToRepository() {
        reservationService.deleteReservation(1);

        verify(reservationRepository).deleteById(1);
    }

    @Test
    @DisplayName("Crée une réservation publique sans participants supplémentaires")
    void addReservation_shouldCreatePublicReservationWhenNoParticipants() {
        MembreEntity organisateur = membre(1, "G0001");
        CourtEntity court = court(1);
        HoraireSiteEntity horaire = horaire(90);

        ReservationDTO inputDto = dto(
                null,
                null,
                List.of()
        );

        ReservationEntity reservationToSave = reservation(null);
        reservationToSave.setDate(inputDto.date());
        reservationToSave.setStartTime(inputDto.startTime());

        ReservationEntity savedReservation = reservation(100);
        savedReservation.setDate(inputDto.date());
        savedReservation.setStartTime(inputDto.startTime());

        when(courtRepository.findById(1)).thenReturn(Optional.of(court));
        when(membreRepository.findById(1)).thenReturn(Optional.of(organisateur));
        when(reservationValidationService.validateReservationPossible(
                court,
                organisateur,
                inputDto.date(),
                inputDto.startTime()
        )).thenReturn(horaire);

        when(reservationMapper.toEntity(inputDto, court, organisateur))
                .thenReturn(reservationToSave);

        when(reservationRepository.save(reservationToSave))
                .thenReturn(savedReservation);

        when(matchRepository.save(any(MatchEntity.class)))
                .thenAnswer(invocation -> {
                    MatchEntity match = invocation.getArgument(0);
                    match.setId(200);
                    return match;
                });

        reservationService.addReservation(inputDto);

        assertEquals(LocalTime.of(19, 30), reservationToSave.getEndTime());

        ArgumentCaptor<MatchEntity> matchCaptor =
                ArgumentCaptor.forClass(MatchEntity.class);

        verify(matchRepository).save(matchCaptor.capture());

        MatchEntity savedMatch = matchCaptor.getValue();

        assertEquals(MatchType.PUBLIC, savedMatch.getTypeMatch());
        assertEquals(MatchStatus.OUVERT, savedMatch.getStatut());
        assertEquals(60, savedMatch.getPrixTotal());
        assertEquals(savedReservation, savedMatch.getReservation());
        assertEquals(court, savedMatch.getTerrain());
        assertEquals(organisateur, savedMatch.getOrganisateur());

        verify(participationRepository, times(1))
                .save(any(ParticipationEntity.class));
    }

    @Test
    @DisplayName("Crée une réservation privée avec participants")
    void addReservation_shouldCreatePrivateReservationWithParticipants() {
        MembreEntity organisateur = membre(1, "G0001");
        MembreEntity joueur2 = membre(2, "G0002");
        MembreEntity joueur3 = membre(3, "G0003");
        MembreEntity joueur4 = membre(4, "G0004");

        CourtEntity court = court(1);
        HoraireSiteEntity horaire = horaire(90);

        ReservationDTO inputDto = dto(
                null,
                null,
                List.of("G0002", "G0003", "G0004")
        );

        ReservationEntity reservationToSave = reservation(null);
        reservationToSave.setDate(inputDto.date());
        reservationToSave.setStartTime(inputDto.startTime());

        ReservationEntity savedReservation = reservation(100);
        savedReservation.setDate(inputDto.date());
        savedReservation.setStartTime(inputDto.startTime());

        when(courtRepository.findById(1)).thenReturn(Optional.of(court));
        when(membreRepository.findById(1)).thenReturn(Optional.of(organisateur));
        when(reservationValidationService.validateReservationPossible(
                court,
                organisateur,
                inputDto.date(),
                inputDto.startTime()
        )).thenReturn(horaire);

        when(reservationMapper.toEntity(inputDto, court, organisateur))
                .thenReturn(reservationToSave);

        when(reservationRepository.save(reservationToSave))
                .thenReturn(savedReservation);

        when(matchRepository.save(any(MatchEntity.class)))
                .thenAnswer(invocation -> {
                    MatchEntity match = invocation.getArgument(0);
                    match.setId(200);
                    return match;
                });

        when(membreRepository.findByMatricule("G0002")).thenReturn(Optional.of(joueur2));
        when(membreRepository.findByMatricule("G0003")).thenReturn(Optional.of(joueur3));
        when(membreRepository.findByMatricule("G0004")).thenReturn(Optional.of(joueur4));

        reservationService.addReservation(inputDto);

        ArgumentCaptor<MatchEntity> matchCaptor =
                ArgumentCaptor.forClass(MatchEntity.class);

        verify(matchRepository).save(matchCaptor.capture());

        MatchEntity savedMatch = matchCaptor.getValue();

        assertEquals(MatchType.PRIVE, savedMatch.getTypeMatch());
        assertEquals(MatchStatus.COMPLET, savedMatch.getStatut());

        ArgumentCaptor<ParticipationEntity> participationCaptor =
                ArgumentCaptor.forClass(ParticipationEntity.class);

        verify(participationRepository, times(4))
                .save(participationCaptor.capture());

        List<ParticipationEntity> participations = participationCaptor.getAllValues();

        assertEquals(4, participations.size());
        assertTrue(participations.stream().allMatch(
                p -> p.getStatut() == ParticipationStatut.EN_ATTENTE_PAIEMENT
        ));
        assertTrue(participations.stream().allMatch(
                p -> p.getMontantDuCentimes() == 1500
        ));
    }

    @Test
    @DisplayName("Refuse la création si le terrain est introuvable")
    void addReservation_shouldThrow404WhenCourtDoesNotExist() {
        ReservationDTO inputDto = dto(null, null, List.of());

        when(courtRepository.findById(1)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> reservationService.addReservation(inputDto)
        );

        assertEquals(404, ex.getStatusCode().value());

        verify(reservationRepository, never()).save(any());
        verify(matchRepository, never()).save(any());
    }

    @Test
    @DisplayName("Refuse la création si le membre est introuvable")
    void addReservation_shouldThrow404WhenMemberDoesNotExist() {
        ReservationDTO inputDto = dto(null, null, List.of());
        CourtEntity court = court(1);

        when(courtRepository.findById(1)).thenReturn(Optional.of(court));
        when(membreRepository.findById(1)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> reservationService.addReservation(inputDto)
        );

        assertEquals(404, ex.getStatusCode().value());

        verify(reservationRepository, never()).save(any());
        verify(matchRepository, never()).save(any());
    }

    @Test
    @DisplayName("Refuse la création si un participant est introuvable")
    void addReservation_shouldThrow404WhenParticipantDoesNotExist() {
        MembreEntity organisateur = membre(1, "G0001");
        CourtEntity court = court(1);
        HoraireSiteEntity horaire = horaire(90);

        ReservationDTO inputDto = dto(null, null, List.of("G9999"));

        ReservationEntity reservationToSave = reservation(null);
        ReservationEntity savedReservation = reservation(100);

        when(courtRepository.findById(1)).thenReturn(Optional.of(court));
        when(membreRepository.findById(1)).thenReturn(Optional.of(organisateur));
        when(reservationValidationService.validateReservationPossible(
                court,
                organisateur,
                inputDto.date(),
                inputDto.startTime()
        )).thenReturn(horaire);

        when(reservationMapper.toEntity(inputDto, court, organisateur))
                .thenReturn(reservationToSave);

        when(reservationRepository.save(reservationToSave))
                .thenReturn(savedReservation);

        when(matchRepository.save(any(MatchEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(membreRepository.findByMatricule("G9999"))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> reservationService.addReservation(inputDto)
        );

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("Annule une réservation existante et délègue à ReservationCancellationService")
    void cancelReservation_shouldDelegateCancellationWhenAllowed() {
        ReservationEntity reservation = reservation(1);
        reservation.setStatut(ReservationStatus.VALIDEE);

        when(reservationRepository.findById(1)).thenReturn(Optional.of(reservation));

        reservationService.cancelReservation(1);

        verify(reservationCancellationService).cancelReservationByMember(reservation);
    }

    @Test
    @DisplayName("Refuse l'annulation si la réservation est introuvable")
    void cancelReservation_shouldThrow404WhenReservationDoesNotExist() {
        when(reservationRepository.findById(999)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> reservationService.cancelReservation(999)
        );

        assertEquals(404, ex.getStatusCode().value());

        verify(reservationCancellationService, never())
                .cancelReservationByMember(any());
    }

    @Test
    @DisplayName("Refuse l'annulation si la réservation est déjà annulée")
    void cancelReservation_shouldThrow409WhenAlreadyCancelled() {
        ReservationEntity reservation = reservation(1);
        reservation.setStatut(ReservationStatus.ANNULEE);

        when(reservationRepository.findById(1)).thenReturn(Optional.of(reservation));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> reservationService.cancelReservation(1)
        );

        assertEquals(409, ex.getStatusCode().value());

        verify(reservationCancellationService, never())
                .cancelReservationByMember(any());
    }

    @Test
    @DisplayName("Refuse l'annulation si la réservation est terminée")
    void cancelReservation_shouldThrow409WhenFinished() {
        ReservationEntity reservation = reservation(1);
        reservation.setStatut(ReservationStatus.TERMINEE);

        when(reservationRepository.findById(1)).thenReturn(Optional.of(reservation));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> reservationService.cancelReservation(1)
        );

        assertEquals(409, ex.getStatusCode().value());

        verify(reservationCancellationService, never())
                .cancelReservationByMember(any());
    }

    private ReservationDTO dto(
            Integer id,
            ReservationStatus status,
            List<String> participants
    ) {
        return new ReservationDTO(
                id,
                null,
                LocalDate.of(2026, 6, 1),
                null,
                LocalTime.of(18, 0),
                1,
                "Site test",
                1,
                status,
                null,
                null,
                participants
        );
    }

    private ReservationEntity reservation(Integer id) {
        ReservationEntity reservation = new ReservationEntity();
        reservation.setId(id);
        reservation.setDate(LocalDate.of(2026, 6, 1));
        reservation.setStartTime(LocalTime.of(18, 0));
        reservation.setEndTime(LocalTime.of(19, 30));
        reservation.setStatut(ReservationStatus.EN_ATTENTE_PAIEMENT);
        return reservation;
    }

    private CourtEntity court(Integer id) {
        SiteEntity site = new SiteEntity();
        site.setId(10);
        site.setNom("Site test");
        site.setActif(true);

        CourtEntity court = new CourtEntity();
        court.setId(id);
        court.setNom("Terrain " + id);
        court.setActif(true);
        court.setSite(site);

        return court;
    }

    private MembreEntity membre(Integer id, String matricule) {
        MembreEntity membre = new MembreEntity();
        membre.setId(id);
        membre.setMatricule(matricule);
        membre.setActif(true);
        membre.setPrenom("Prenom" + id);
        membre.setNom("Nom" + id);
        return membre;
    }

    private HoraireSiteEntity horaire(int dureeMinutes) {
        HoraireSiteEntity horaire = new HoraireSiteEntity();
        horaire.setDuree_match_minutes(dureeMinutes);
        return horaire;
    }
}