package be.angularpadelclub.Service;

import be.angularpadelclub.DTO.CourtDTO;
import be.angularpadelclub.Mapper.CourtMapper;
import be.angularpadelclub.Repository.CourtRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourtService {

    private final CourtRepository courtRepository;

    public CourtService(CourtRepository courtRepository) {
        this.courtRepository = courtRepository;
    }

    public List<CourtDTO> getCourtsBySiteId(Integer siteId) {
        return courtRepository.findBySiteId(siteId)
                .stream()
                .map(CourtMapper::toDTO)
                .toList();
    }
}