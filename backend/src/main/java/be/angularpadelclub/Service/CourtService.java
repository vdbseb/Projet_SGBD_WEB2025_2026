package be.angularpadelclub.Service;

import be.angularpadelclub.DTO.CourtDTO;
import be.angularpadelclub.Entity.CourtEntity;
import be.angularpadelclub.Entity.SiteEntity;
import be.angularpadelclub.Mapper.CourtMapper;
import be.angularpadelclub.Repository.CourtRepository;
import be.angularpadelclub.Repository.ReservationRepository;
import be.angularpadelclub.Repository.SiteRepository;
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
    private final CourtMapper courtMapper;

    public CourtService(CourtRepository courtRepository,
                        SiteRepository siteRepository,
                        ReservationRepository reservationRepository,
                        CourtMapper courtMapper) {
        this.courtRepository = courtRepository;
        this.siteRepository = siteRepository;
        this.reservationRepository = reservationRepository;
        this.courtMapper = courtMapper;
    }

    public List<CourtDTO> getAllCourts() {
        return courtRepository.findByActifTrue()
                .stream()
                .map(courtMapper::toDTO)
                .toList();
    }

    public CourtDTO getCourtById(int id) {
        CourtEntity court = courtRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Terrain introuvable avec l'id : " + id
                ));

        return courtMapper.toDTO(court);
    }

    public CourtDTO createCourt(CourtDTO dto) {
        SiteEntity site = siteRepository.findById(dto.siteId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Site introuvable avec l'id : " + dto.siteId()
                ));

        if (!site.isActif()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Impossible d'ajouter un terrain à un site désactivé."
            );
        }

        CourtEntity court = courtMapper.toEntity(dto, site);
        court.setActif(true);

        CourtEntity savedCourt = courtRepository.save(court);

        return courtMapper.toDTO(savedCourt);
    }

    public CourtDTO updateCourt(int id, CourtDTO dto) {
        CourtEntity court = courtRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Terrain introuvable avec l'id : " + id
                ));

        SiteEntity site = siteRepository.findById(dto.siteId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Site introuvable avec l'id : " + dto.siteId()
                ));

        if (!site.isActif()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Impossible d'affecter un terrain à un site désactivé."
            );
        }

        court.setNom(dto.name());
      //  court.setDescription(dto.description()); => pas de description encore ? peut-être jamais
        court.setSite(site);

        CourtEntity updatedCourt = courtRepository.save(court);

        return courtMapper.toDTO(updatedCourt);
    }

    public void deleteCourt(int id) {
        CourtEntity court = courtRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Terrain introuvable avec l'id : " + id
                ));

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
}