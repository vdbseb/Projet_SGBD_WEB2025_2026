package be.angularpadelclub.Service;

import be.angularpadelclub.DTO.HoraireSiteDTO;
import be.angularpadelclub.Entity.HoraireSiteEntity;
import be.angularpadelclub.Entity.SiteEntity;
import be.angularpadelclub.Repository.HoraireSiteRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class HoraireSiteService {

    private static final int MIN_YEAR = 2020;

    private final HoraireSiteRepository horaireSiteRepository;
    private final ReferenceLookupService referenceLookupService;

    public HoraireSiteService(
            HoraireSiteRepository horaireSiteRepository,
            ReferenceLookupService referenceLookupService
    ) {
        this.horaireSiteRepository = horaireSiteRepository;
        this.referenceLookupService = referenceLookupService;
    }

    public List<HoraireSiteEntity> findAll() {
        return horaireSiteRepository.findAll();
    }

    public HoraireSiteEntity findById(Integer id) {
        return referenceLookupService.findHoraireSiteOrThrow(id);
    }

    public List<HoraireSiteEntity> findBySiteId(Integer siteId) {
        referenceLookupService.findSiteOrThrow(siteId);

        return horaireSiteRepository.findBySite_Id(siteId);
    }

    public HoraireSiteEntity findBySiteIdAndAnnee(
            Integer siteId,
            int annee
    ) {
        referenceLookupService.findSiteOrThrow(siteId);

        return horaireSiteRepository.findBySite_IdAndAnnee(siteId, annee)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Aucun horaire trouvé pour le site " + siteId
                                + " et l'année " + annee + "."
                ));
    }

    @Transactional
    public HoraireSiteEntity create(HoraireSiteDTO dto) {
        validate(dto);

        SiteEntity site = referenceLookupService.findSiteOrThrow(dto.siteId());

        ensureNoDuplicateForCreate(
                dto.siteId(),
                dto.annee()
        );

        HoraireSiteEntity horaire = new HoraireSiteEntity();

        horaire.setSite(site);
        horaire.setAnnee(dto.annee());
        horaire.setHeure_debut(dto.heure_debut());
        horaire.setHeure_fin(dto.heure_fin());
        horaire.setDuree_match_minutes(dto.duree_match_minutes());
        horaire.setPause_minutes(dto.pause_minutes());

        return horaireSiteRepository.save(horaire);
    }

    @Transactional
    public HoraireSiteEntity update(
            Integer id,
            HoraireSiteDTO dto
    ) {
        validate(dto);

        HoraireSiteEntity horaire = referenceLookupService.findHoraireSiteOrThrow(id);
        SiteEntity site = referenceLookupService.findSiteOrThrow(dto.siteId());

        ensureNoDuplicateForUpdate(
                id,
                dto.siteId(),
                dto.annee()
        );

        horaire.setSite(site);
        horaire.setAnnee(dto.annee());
        horaire.setHeure_debut(dto.heure_debut());
        horaire.setHeure_fin(dto.heure_fin());
        horaire.setDuree_match_minutes(dto.duree_match_minutes());
        horaire.setPause_minutes(dto.pause_minutes());

        return horaireSiteRepository.save(horaire);
    }

    @Transactional
    public void delete(Integer id) {
        HoraireSiteEntity horaire = referenceLookupService.findHoraireSiteOrThrow(id);
        horaireSiteRepository.delete(horaire);
    }

    private void ensureNoDuplicateForCreate(
            Integer siteId,
            int annee
    ) {
        if (horaireSiteRepository.existsBySite_IdAndAnnee(siteId, annee)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Un horaire existe déjà pour le site " + siteId
                            + " et l'année " + annee + "."
            );
        }
    }

    private void ensureNoDuplicateForUpdate(
            Integer currentHoraireId,
            Integer siteId,
            int annee
    ) {
        horaireSiteRepository.findBySite_IdAndAnnee(siteId, annee)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(currentHoraireId)) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Un autre horaire existe déjà pour le site "
                                        + siteId + " et l'année " + annee + "."
                        );
                    }
                });
    }

    private void validate(HoraireSiteDTO dto) {
        if (dto == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le corps de la requête est obligatoire."
            );
        }

        if (dto.siteId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le site de l'horaire est obligatoire."
            );
        }

        if (dto.annee() < MIN_YEAR) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "L'année de l'horaire doit être supérieure ou égale à "
                            + MIN_YEAR + "."
            );
        }

        if (dto.heure_debut() == null || dto.heure_fin() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Les heures de début et de fin sont obligatoires."
            );
        }

        if (!dto.heure_debut().isBefore(dto.heure_fin())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "L'heure de début doit être strictement avant l'heure de fin."
            );
        }

        if (dto.duree_match_minutes()
                != ClubBusinessRules.DEFAULT_MATCH_DURATION_MINUTES) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La durée d'un match doit être de "
                            + ClubBusinessRules.DEFAULT_MATCH_DURATION_MINUTES
                            + " minutes."
            );
        }

        if (dto.pause_minutes()
                != ClubBusinessRules.DEFAULT_PAUSE_MINUTES) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La pause entre deux matches doit être de "
                            + ClubBusinessRules.DEFAULT_PAUSE_MINUTES
                            + " minutes."
            );
        }
    }
}