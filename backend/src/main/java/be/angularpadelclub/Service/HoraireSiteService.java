package be.angularpadelclub.Service;

import be.angularpadelclub.DTO.HoraireSiteDTO;
import be.angularpadelclub.Entity.HoraireSiteEntity;
import be.angularpadelclub.Entity.SiteEntity;
import be.angularpadelclub.Repository.HoraireSiteRepository;
import be.angularpadelclub.Repository.SiteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class HoraireSiteService {

    private final HoraireSiteRepository horaireSiteRepository;
    private final SiteRepository siteRepository;

    public HoraireSiteService(
            HoraireSiteRepository horaireSiteRepository,
            SiteRepository siteRepository
    ) {
        this.horaireSiteRepository = horaireSiteRepository;
        this.siteRepository = siteRepository;
    }

    public List<HoraireSiteEntity> findAll() {
        return horaireSiteRepository.findAll();
    }

    public HoraireSiteEntity findById(Integer id) {
        return horaireSiteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Horaire de site introuvable avec l'id : " + id
                ));
    }

    public List<HoraireSiteEntity> findBySiteId(Integer siteId) {
        return horaireSiteRepository.findBySite_Id(siteId);
    }

    public HoraireSiteEntity findBySiteIdAndAnnee(Integer siteId, int annee) {
        return horaireSiteRepository.findBySite_IdAndAnnee(siteId, annee)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Aucun horaire trouvé pour le site " + siteId + " et l'année " + annee
                ));
    }

    public HoraireSiteEntity create(HoraireSiteDTO dto) {
        validate(dto);

        if (horaireSiteRepository.existsBySite_IdAndAnnee(dto.siteId(), dto.annee())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Un horaire existe déjà pour le site " + dto.siteId()
                            + " et l'année " + dto.annee()
            );
        }

        SiteEntity site = siteRepository.findById(dto.siteId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Site introuvable avec l'id : " + dto.siteId()
                ));

        HoraireSiteEntity horaire = new HoraireSiteEntity();
        horaire.setSite(site);
        horaire.setAnnee(dto.annee());
        horaire.setHeure_debut(dto.heure_debut());
        horaire.setHeure_fin(dto.heure_fin());
        horaire.setDuree_match_minutes(dto.duree_match_minutes());
        horaire.setPause_minutes(dto.pause_minutes());

        return horaireSiteRepository.save(horaire);
    }

    public HoraireSiteEntity update(Integer id, HoraireSiteDTO dto) {
        validate(dto);

        HoraireSiteEntity horaire = findById(id);

        SiteEntity site = siteRepository.findById(dto.siteId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Site introuvable avec l'id : " + dto.siteId()
                ));

        horaireSiteRepository.findBySite_IdAndAnnee(dto.siteId(), dto.annee())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Un autre horaire existe déjà pour le site "
                                        + dto.siteId() + " et l'année " + dto.annee()
                        );
                    }
                });

        horaire.setSite(site);
        horaire.setAnnee(dto.annee());
        horaire.setHeure_debut(dto.heure_debut());
        horaire.setHeure_fin(dto.heure_fin());
        horaire.setDuree_match_minutes(dto.duree_match_minutes());
        horaire.setPause_minutes(dto.pause_minutes());

        return horaireSiteRepository.save(horaire);
    }

    public void delete(Integer id) {
        if (!horaireSiteRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Horaire de site introuvable avec l'id : " + id
            );
        }

        horaireSiteRepository.deleteById(id);
    }

    private void validate(HoraireSiteDTO dto) {
        if (dto == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le corps de la requête est obligatoire"
            );
        }

        if (dto.siteId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le site est obligatoire"
            );
        }

        if (dto.annee() < 2020) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "L'année est invalide"
            );
        }

        if (dto.heure_debut() == null || dto.heure_fin() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Les heures de début et de fin sont obligatoires"
            );
        }

        if (!dto.heure_debut().isBefore(dto.heure_fin())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "L'heure de début doit être avant l'heure de fin"
            );
        }

        if (dto.duree_match_minutes() != 90) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La durée d'un match doit être de 90 minutes"
            );
        }

        if (dto.pause_minutes() != 15) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La pause entre deux matchs doit être de 15 minutes"
            );
        }
    }
}