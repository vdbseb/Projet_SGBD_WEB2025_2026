package be.angularpadelclub.Service;

import be.angularpadelclub.DTO.CourtDTO;
import be.angularpadelclub.Entity.CourtEntity;
import be.angularpadelclub.Entity.ReservationEntity;
import be.angularpadelclub.Entity.SiteEntity;
import be.angularpadelclub.Mapper.CourtMapper;
import be.angularpadelclub.Repository.CourtRepository;
import be.angularpadelclub.Repository.ReservationRepository;
import be.angularpadelclub.Repository.SiteRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class CourtService {

    private final CourtRepository courtRepository;
    private final SiteRepository siteRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationCancellationService reservationCancellationService;
    private final CourtMapper courtMapper;

    public CourtService(
            CourtRepository courtRepository,
            SiteRepository siteRepository,
            ReservationRepository reservationRepository,
            ReservationCancellationService reservationCancellationService,
            CourtMapper courtMapper
    ) {
        this.courtRepository = courtRepository;
        this.siteRepository = siteRepository;
        this.reservationRepository = reservationRepository;
        this.reservationCancellationService = reservationCancellationService;
        this.courtMapper = courtMapper;
    }

    public List<CourtDTO> getAllCourts() {
        return courtRepository.findByActifTrue()
                .stream()
                .map(courtMapper::toDTO)
                .toList();
    }

    public CourtDTO getCourtById(int id) {
        CourtEntity court = findCourtOrThrow(id);

        return courtMapper.toDTO(court);
    }

    @Transactional
    public CourtDTO createCourt(CourtDTO dto) {
        SiteEntity site = findSiteOrThrow(dto.siteId());

        if (!site.isActif()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Impossible d'ajouter un terrain à un site désactivé."
            );
        }

        CourtEntity court = courtMapper.toEntity(dto, site);
        court.setActif(true);
        court.setMaintenance(false);

        CourtEntity savedCourt = courtRepository.save(court);

        return courtMapper.toDTO(savedCourt);
    }

    @Transactional
    public CourtDTO updateCourt(int id, CourtDTO dto) {
        CourtEntity court = findCourtOrThrow(id);
        SiteEntity site = findSiteOrThrow(dto.siteId());

        if (!site.isActif()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Impossible d'affecter un terrain à un site désactivé."
            );
        }

        court.setNom(dto.name());
        court.setSite(site);
        court.setCouvert(dto.indoor());
        court.setActif(dto.active());

        CourtEntity updatedCourt = courtRepository.save(court);

        return courtMapper.toDTO(updatedCourt);
    }

    @Transactional
    public CourtDTO setMaintenance(
            int id,
            boolean maintenance
    ) {
        CourtEntity court = findCourtOrThrow(id);

        if (!court.isActif()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Impossible de modifier la maintenance d'un terrain désactivé."
            );
        }

        court.setMaintenance(maintenance);

        CourtEntity savedCourt = courtRepository.save(court);

        if (maintenance) {
            List<ReservationEntity> impactedReservations =
                    reservationRepository.findByCourtIdAndDateGreaterThanEqual(
                            id,
                            LocalDate.now()
                    );

            reservationCancellationService.cancelReservationsForClubReason(
                    impactedReservations
            );
        }

        return courtMapper.toDTO(savedCourt);
    }

    @Transactional
    public void deleteCourt(int id) {
        CourtEntity court = findCourtOrThrow(id);

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

    private CourtEntity findCourtOrThrow(int id) {
        return courtRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Terrain introuvable avec l'id : " + id
                ));
    }

    private SiteEntity findSiteOrThrow(Integer siteId) {
        return siteRepository.findById(siteId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Site introuvable avec l'id : " + siteId
                ));
    }
}