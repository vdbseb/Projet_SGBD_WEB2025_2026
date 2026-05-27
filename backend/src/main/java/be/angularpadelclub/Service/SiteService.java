package be.angularpadelclub.Service;

import be.angularpadelclub.DTO.SiteDTO;
import be.angularpadelclub.Entity.HoraireSiteEntity;
import be.angularpadelclub.Entity.SiteEntity;
import be.angularpadelclub.Mapper.SiteMapper;
import be.angularpadelclub.Repository.HoraireSiteRepository;
import be.angularpadelclub.Repository.SiteRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class SiteService {

    private static final int DEFAULT_DUREE_MATCH_MINUTES = 90;
    private static final int DEFAULT_PAUSE_MINUTES = 15;

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

        return siteRepository.findAll()
                .stream()
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

        SiteEntity site = findSiteOrThrow(id);

        HoraireSiteEntity horaire = horaireSiteRepository
                .findBySite_IdAndAnnee(site.getId(), currentYear)
                .orElse(null);

        return siteMapper.toDTO(site, horaire);
    }

    @Transactional
    public SiteDTO createSite(SiteDTO dto) {
        validateRequiredCreateFields(dto);

        SiteEntity entity = siteMapper.toEntity(dto);
        entity.setId(null);

        SiteEntity savedSite = siteRepository.save(entity);

        HoraireSiteEntity horaire = new HoraireSiteEntity();
        horaire.setSite(savedSite);
        horaire.setAnnee(LocalDate.now().getYear());
        horaire.setHeure_debut(dto.openingTime());
        horaire.setHeure_fin(dto.closingTime());
        horaire.setDuree_match_minutes(DEFAULT_DUREE_MATCH_MINUTES);
        horaire.setPause_minutes(DEFAULT_PAUSE_MINUTES);

        HoraireSiteEntity savedHoraire =
                horaireSiteRepository.save(horaire);

        return siteMapper.toDTO(savedSite, savedHoraire);
    }

    @Transactional
    public SiteDTO updateSite(int id, SiteDTO dto) {
        SiteEntity site = findSiteOrThrow(id);

        if (dto.name() != null) {
            site.setNom(dto.name());
        }

        if (dto.adresse() != null) {
            site.setAdresse(dto.adresse());
        }

        if (dto.city() != null) {
            site.setVille(dto.city());
        }

        if (dto.codePostal() != null) {
            site.setCode_postal(dto.codePostal());
        }

        if (dto.description() != null) {
            site.setDescription(dto.description());
        }

        if (dto.active() != null) {
            site.setActif(dto.active());
        }

        if (dto.imageUrl() != null) {
            site.setImage_url(dto.imageUrl());
        }

        SiteEntity updatedSite = siteRepository.save(site);

        int currentYear = LocalDate.now().getYear();

        HoraireSiteEntity horaire = horaireSiteRepository
                .findBySite_IdAndAnnee(site.getId(), currentYear)
                .orElseGet(() -> {
                    HoraireSiteEntity h = new HoraireSiteEntity();
                    h.setSite(site);
                    h.setAnnee(currentYear);
                    h.setDuree_match_minutes(DEFAULT_DUREE_MATCH_MINUTES);
                    h.setPause_minutes(DEFAULT_PAUSE_MINUTES);
                    return h;
                });

        if (dto.openingTime() != null) {
            horaire.setHeure_debut(dto.openingTime());
        }

        if (dto.closingTime() != null) {
            horaire.setHeure_fin(dto.closingTime());
        }

        HoraireSiteEntity updatedHoraire =
                horaireSiteRepository.save(horaire);

        return siteMapper.toDTO(updatedSite, updatedHoraire);
    }

    @Transactional
    public void deleteSite(int id) {
        SiteEntity site = findSiteOrThrow(id);

        if (!site.isActif()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Le site est déjà désactivé."
            );
        }

        site.setActif(false);
        siteRepository.save(site);
    }

    private SiteEntity findSiteOrThrow(int id) {
        return siteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Site introuvable avec l'id " + id
                ));
    }

    private void validateRequiredCreateFields(SiteDTO dto) {
        if (dto.name() == null || dto.name().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le nom du site est obligatoire."
            );
        }

        if (dto.adresse() == null || dto.adresse().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "L'adresse du site est obligatoire."
            );
        }

        if (dto.city() == null || dto.city().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La ville du site est obligatoire."
            );
        }

        if (dto.codePostal() == null || dto.codePostal().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le code postal du site est obligatoire."
            );
        }

        if (dto.openingTime() == null || dto.closingTime() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Les heures d'ouverture et de fermeture sont obligatoires."
            );
        }

        if (!dto.openingTime().isBefore(dto.closingTime())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "L'heure d'ouverture doit être avant l'heure de fermeture."
            );
        }
    }
}