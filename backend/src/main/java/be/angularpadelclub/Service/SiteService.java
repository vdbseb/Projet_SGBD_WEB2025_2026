package be.angularpadelclub.Service;

import be.angularpadelclub.DTO.SiteDTO;
import be.angularpadelclub.Entity.HoraireSiteEntity;
import be.angularpadelclub.Entity.SiteEntity;
import be.angularpadelclub.Mapper.SiteMapper;
import be.angularpadelclub.Repository.HoraireSiteRepository;
import be.angularpadelclub.Repository.SiteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Service
public class SiteService {

    private final SiteRepository siteRepository;
    private final HoraireSiteRepository horaireSiteRepository;
    private final SiteMapper siteMapper;

    public SiteService(
            SiteRepository siteRepository,
            HoraireSiteRepository horaireSiteRepository,
            SiteMapper siteMapper
    ) {
        this.siteRepository = siteRepository;
        this.horaireSiteRepository = horaireSiteRepository;
        this.siteMapper = siteMapper;
    }

    public List<SiteDTO> getAllSites() {
        int currentYear = LocalDate.now().getYear();

        return siteRepository.findAll().stream()
                .map(site -> {
                    HoraireSiteEntity horaire = horaireSiteRepository
                            .findBySite_IdAndAnnee(site.getId(), currentYear)
                            .orElse(null);

                    return siteMapper.toDTO(site, horaire);
                })
                .toList();
    }

    public SiteDTO getSiteById(int id) {
        int currentYear = LocalDate.now().getYear();
        SiteEntity site = siteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Site not found"));

        HoraireSiteEntity horaire = horaireSiteRepository
                .findBySite_IdAndAnnee(site.getId(), currentYear)
                .orElse(null);

        return siteMapper.toDTO(site, horaire);
    }

    public SiteDTO createSite(SiteDTO dto) {
        SiteEntity entity = siteMapper.toEntity(dto);
        entity.setId(null);

        SiteEntity savedSite = siteRepository.save(entity);

        HoraireSiteEntity horaire = new HoraireSiteEntity();
        horaire.setSite(savedSite);
        horaire.setAnnee(LocalDate.now().getYear());
        horaire.setHeure_debut(dto.openingTime());
        horaire.setHeure_fin(dto.closingTime());
        horaire.setDuree_match_minutes(90);
        horaire.setPause_minutes(15);

        HoraireSiteEntity savedHoraire = horaireSiteRepository.save(horaire);

        return siteMapper.toDTO(savedSite, savedHoraire);
    }

    public void deleteSite(int id) {
        siteRepository.deleteById(id);
    }
}
