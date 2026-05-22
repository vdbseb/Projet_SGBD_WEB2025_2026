package be.angularpadelclub.Service;

import be.angularpadelclub.DTO.CourtDTO;
import be.angularpadelclub.Entity.CourtEntity;
import be.angularpadelclub.Entity.SiteEntity;
import be.angularpadelclub.Mapper.CourtMapper;
import be.angularpadelclub.Repository.CourtRepository;
import be.angularpadelclub.Repository.SiteRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class CourtService {

    private final CourtRepository courtRepository;
    private final SiteRepository siteRepository;
    private final CourtMapper courtMapper;

    public CourtService(CourtRepository courtRepository,
                        SiteRepository siteRepository,
                        CourtMapper courtMapper) {
        this.courtRepository = courtRepository;
        this.siteRepository = siteRepository;
        this.courtMapper = courtMapper;
    }

    public List<CourtDTO> getAllCourts() {
        return courtRepository.findAll()
                .stream()
                .map(courtMapper::toDTO)
                .toList();
    }

    public CourtDTO getCourtById(int id) {
        CourtEntity court = courtRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Court not found"));

        return courtMapper.toDTO(court);
    }

    public CourtDTO createCourt(CourtDTO dto) {
        SiteEntity site = siteRepository.findById(dto.siteId())
                .orElseThrow(() -> new RuntimeException("Site not found"));

        CourtEntity court = courtMapper.toEntity(dto, site);
        CourtEntity savedCourt = courtRepository.save(court);

        return courtMapper.toDTO(savedCourt);
    }

    public void deleteCourt(int id) {
        courtRepository.deleteById(id);
    }
}