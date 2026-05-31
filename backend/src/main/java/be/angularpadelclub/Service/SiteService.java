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

    private final SiteRepository siteRepository;
    private final HoraireSiteRepository horaireSiteRepository;
    private final SiteMapper siteMapper;
    private final ReferenceLookupService referenceLookupService;

    public SiteService(
            SiteRepository siteRepository,
            HoraireSiteRepository horaireSiteRepository,
            SiteMapper siteMapper,
            ReferenceLookupService referenceLookupService
    ) {
        this.siteRepository = siteRepository;
        this.horaireSiteRepository = horaireSiteRepository;
        this.siteMapper = siteMapper;
        this.referenceLookupService = referenceLookupService;
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

        SiteEntity site = referenceLookupService.findSiteOrThrow(id);

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

        if (dto.active() == null) {
            entity.setActif(true);
        }

        SiteEntity savedSite = siteRepository.save(entity);

        HoraireSiteEntity horaire = buildDefaultHoraireForSite(
                savedSite,
                dto
        );

        HoraireSiteEntity savedHoraire =
                horaireSiteRepository.save(horaire);

        return siteMapper.toDTO(savedSite, savedHoraire);
    }

    @Transactional
    public SiteDTO updateSite(int id, SiteDTO dto) {
        if (dto == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Les données du site sont obligatoires."
            );
        }

        SiteEntity site = referenceLookupService.findSiteOrThrow(id);

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

        HoraireSiteEntity updatedHoraire = updateCurrentYearHoraire(
                updatedSite,
                dto
        );

        return siteMapper.toDTO(updatedSite, updatedHoraire);
    }

    @Transactional
    public void deleteSite(int id) {
        SiteEntity site = referenceLookupService.findSiteOrThrow(id);

        if (!site.isActif()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Le site est déjà désactivé."
            );
        }

        site.setActif(false);
        siteRepository.save(site);
    }

    @Transactional
    public SiteDTO reactivateSite(int id) {
        SiteEntity site = referenceLookupService.findSiteOrThrow(id);

        if (site.isActif()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Le site est déjà actif."
            );
        }

        site.setActif(true);

        SiteEntity updatedSite = siteRepository.save(site);

        int currentYear = LocalDate.now().getYear();

        HoraireSiteEntity horaire = horaireSiteRepository
                .findBySite_IdAndAnnee(updatedSite.getId(), currentYear)
                .orElse(null);

        return siteMapper.toDTO(updatedSite, horaire);
    }

    private HoraireSiteEntity buildDefaultHoraireForSite(
            SiteEntity site,
            SiteDTO dto
    ) {
        HoraireSiteEntity horaire = new HoraireSiteEntity();

        horaire.setSite(site);
        horaire.setAnnee(LocalDate.now().getYear());
        horaire.setHeure_debut(dto.openingTime());
        horaire.setHeure_fin(dto.closingTime());
        horaire.setDuree_match_minutes(
                ClubBusinessRules.DEFAULT_MATCH_DURATION_MINUTES
        );
        horaire.setPause_minutes(
                ClubBusinessRules.DEFAULT_PAUSE_MINUTES
        );

        return horaire;
    }

    private HoraireSiteEntity updateCurrentYearHoraire(
            SiteEntity site,
            SiteDTO dto
    ) {
        int currentYear = LocalDate.now().getYear();

        HoraireSiteEntity horaire = horaireSiteRepository
                .findBySite_IdAndAnnee(site.getId(), currentYear)
                .orElseGet(() -> createHoraireFromUpdateRequest(site, dto, currentYear));

        if (dto.openingTime() != null) {
            horaire.setHeure_debut(dto.openingTime());
        }

        if (dto.closingTime() != null) {
            horaire.setHeure_fin(dto.closingTime());
        }

        validateHoraireIsComplete(horaire);

        if (!horaire.getHeure_debut().isBefore(horaire.getHeure_fin())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "L'heure d'ouverture doit être avant l'heure de fermeture."
            );
        }

        return horaireSiteRepository.save(horaire);
    }

    private HoraireSiteEntity createHoraireFromUpdateRequest(
            SiteEntity site,
            SiteDTO dto,
            int currentYear
    ) {
        if (dto.openingTime() == null || dto.closingTime() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Impossible de créer l'horaire du site : les heures d'ouverture et de fermeture sont obligatoires."
            );
        }

        HoraireSiteEntity horaire = new HoraireSiteEntity();

        horaire.setSite(site);
        horaire.setAnnee(currentYear);
        horaire.setHeure_debut(dto.openingTime());
        horaire.setHeure_fin(dto.closingTime());
        horaire.setDuree_match_minutes(
                ClubBusinessRules.DEFAULT_MATCH_DURATION_MINUTES
        );
        horaire.setPause_minutes(
                ClubBusinessRules.DEFAULT_PAUSE_MINUTES
        );

        return horaire;
    }

    private void validateHoraireIsComplete(HoraireSiteEntity horaire) {
        if (horaire.getHeure_debut() == null || horaire.getHeure_fin() == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "L'horaire du site est incomplet : les heures d'ouverture et de fermeture sont obligatoires."
            );
        }
    }

    private void validateRequiredCreateFields(SiteDTO dto) {
        if (dto == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Les données du site sont obligatoires."
            );
        }

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