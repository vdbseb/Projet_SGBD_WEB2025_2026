package be.angularpadelclub.Service;

import be.angularpadelclub.DTO.SiteDTO;
import be.angularpadelclub.Entity.SiteEntity;
import be.angularpadelclub.Entity.CourtEntity;
import be.angularpadelclub.Mapper.SiteMapper;
import be.angularpadelclub.Repository.SiteRepository;
import be.angularpadelclub.Repository.CourtRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SiteService {

    private final SiteRepository siteRepository;
    private final CourtRepository courtRepository;

    public SiteService(
            SiteRepository siteRepository,
            CourtRepository courtRepository
    ) {
        this.siteRepository = siteRepository;
        this.courtRepository = courtRepository;
    }

    public List<SiteDTO> getAllSites() {
        return siteRepository.findAll()
                .stream()
                .map(site -> {
                    List<CourtEntity> courts =
                            courtRepository.findBySiteId(site.getId());

                    return SiteMapper.toDTO(site, courts);
                })
                .toList();
    }
}