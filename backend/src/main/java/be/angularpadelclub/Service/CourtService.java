package be.angularpadelclub.Service;

import be.angularpadelclub.DTO.CourtDTO;
import be.angularpadelclub.Entity.CourtEntity;
import be.angularpadelclub.Entity.ReservationEntity;
import be.angularpadelclub.Entity.SiteEntity;
import be.angularpadelclub.Mapper.CourtMapper;
import be.angularpadelclub.Repository.CourtRepository;
import be.angularpadelclub.Repository.ReservationRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class CourtService {

    private final CourtRepository courtRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationCancellationService reservationCancellationService;
    private final ReferenceLookupService referenceLookupService;
    private final CourtMapper courtMapper;

    public CourtService(
            CourtRepository courtRepository,
            ReservationRepository reservationRepository,
            ReservationCancellationService reservationCancellationService,
            ReferenceLookupService referenceLookupService,
            CourtMapper courtMapper
    ) {
        this.courtRepository = courtRepository;
        this.reservationRepository = reservationRepository;
        this.reservationCancellationService = reservationCancellationService;
        this.referenceLookupService = referenceLookupService;
        this.courtMapper = courtMapper;
    }

    public List<CourtDTO> getAllCourts() {
        return courtRepository.findByActifTrue()
                .stream()
                .map(courtMapper::toDTO)
                .toList();
    }

    public CourtDTO getCourtById(int id) {
        return courtMapper.toDTO(
                referenceLookupService.findCourtOrThrow(id)
        );
    }

    @Transactional
    public CourtDTO createCourt(CourtDTO dto) {
        SiteEntity site = referenceLookupService.findSiteOrThrow(dto.siteId());

        ensureSiteIsActiveForCourtManagement(site);

        CourtEntity court = courtMapper.toEntity(dto, site);
        court.setActif(true);
        court.setMaintenance(false);

        return courtMapper.toDTO(
                courtRepository.save(court)
        );
    }

    @Transactional
    public CourtDTO updateCourt(int id, CourtDTO dto) {
        CourtEntity court = referenceLookupService.findCourtOrThrow(id);
        SiteEntity site = referenceLookupService.findSiteOrThrow(dto.siteId());

        ensureSiteIsActiveForCourtManagement(site);

        court.setNom(dto.name());
        court.setSite(site);
        court.setCouvert(dto.indoor());
        court.setActif(dto.active());

        return courtMapper.toDTO(
                courtRepository.save(court)
        );
    }

    @Transactional
    public CourtDTO setMaintenance(
            int id,
            boolean maintenance
    ) {
        CourtEntity court = referenceLookupService.findCourtOrThrow(id);

        if (!court.isActif()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Impossible de modifier la maintenance d'un terrain désactivé."
            );
        }

        if (court.isMaintenance() == maintenance) {
            return courtMapper.toDTO(court);
        }

        court.setMaintenance(maintenance);

        CourtEntity savedCourt = courtRepository.save(court);

        if (maintenance) {
            cancelFutureReservationsForCourt(id);
        }

        return courtMapper.toDTO(savedCourt);
    }

    @Transactional
    public void deleteCourt(int id) {
        CourtEntity court = referenceLookupService.findCourtOrThrow(id);

        boolean hasFutureReservations =
                reservationRepository.existsByCourtIdAndDateAfter(
                        id,
                        LocalDate.now()
                );

        if (hasFutureReservations) {
            court.setActif(false);
            courtRepository.save(court);

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Le terrain possède des réservations futures. Il a été désactivé au lieu d'être supprimé."
            );
        }

        courtRepository.delete(court);
    }

    private void ensureSiteIsActiveForCourtManagement(SiteEntity site) {
        if (!site.isActif()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Impossible de gérer un terrain sur un site désactivé."
            );
        }
    }

    private void cancelFutureReservationsForCourt(int courtId) {
        List<ReservationEntity> impactedReservations =
                reservationRepository.findByCourtIdAndDateGreaterThanEqual(
                        courtId,
                        LocalDate.now()
                );

        reservationCancellationService.cancelReservationsForClubReason(
                impactedReservations
        );
    }
}