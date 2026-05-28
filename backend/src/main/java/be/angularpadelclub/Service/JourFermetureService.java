package be.angularpadelclub.Service;

import be.angularpadelclub.DTO.JourFermetureDTO;
import be.angularpadelclub.Entity.JourFermetureEntity;
import be.angularpadelclub.Entity.SiteEntity;
import be.angularpadelclub.Repository.JourFermetureRepository;
import be.angularpadelclub.Repository.SiteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class JourFermetureService {

    private final JourFermetureRepository jourFermetureRepository;
    private final SiteRepository siteRepository;

    public JourFermetureService(
            JourFermetureRepository jourFermetureRepository,
            SiteRepository siteRepository
    ) {
        this.jourFermetureRepository = jourFermetureRepository;
        this.siteRepository = siteRepository;
    }

    public List<JourFermetureEntity> findAll() {
        return jourFermetureRepository.findAll();
    }

    public JourFermetureEntity findById(Integer id) {
        return jourFermetureRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Jour de fermeture introuvable avec l'id : " + id
                ));
    }

    public List<JourFermetureEntity> findBySiteId(Integer siteId) {
        return jourFermetureRepository.findBySiteId(siteId);
    }

    public List<JourFermetureEntity> findGlobalClosures() {
        return jourFermetureRepository.findByGlobalTrue();
    }

    public JourFermetureEntity create(JourFermetureDTO dto) {
        validate(dto);

        if (dto.global()) {
            if (jourFermetureRepository.existsByGlobalTrueAndDateFermeture(dto.dateFermeture())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Une fermeture globale existe déjà pour la date : " + dto.dateFermeture()
                );
            }

            JourFermetureEntity fermeture = new JourFermetureEntity();
            fermeture.setSite(null);
            fermeture.setDateFermeture(dto.dateFermeture());
            fermeture.setRaison(dto.raison());
            fermeture.setGlobal(true);

            return jourFermetureRepository.save(fermeture);
        }

        SiteEntity site = siteRepository.findById(dto.siteId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Site introuvable avec l'id : " + dto.siteId()
                ));

        if (jourFermetureRepository.existsBySiteIdAndDateFermeture(
                dto.siteId(),
                dto.dateFermeture()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Une fermeture existe déjà pour le site "
                            + dto.siteId()
                            + " à la date : "
                            + dto.dateFermeture()
            );
        }

        JourFermetureEntity fermeture = new JourFermetureEntity();
        fermeture.setSite(site);
        fermeture.setDateFermeture(dto.dateFermeture());
        fermeture.setRaison(dto.raison());
        fermeture.setGlobal(false);

        return jourFermetureRepository.save(fermeture);
    }

    public JourFermetureEntity update(Integer id, JourFermetureDTO dto) {
        validate(dto);

        JourFermetureEntity fermeture = findById(id);

        if (dto.global()) {
            jourFermetureRepository
                    .findByGlobalTrueAndDateFermeture(dto.dateFermeture())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new ResponseStatusException(
                                    HttpStatus.CONFLICT,
                                    "Une autre fermeture globale existe déjà pour la date : "
                                            + dto.dateFermeture()
                            );
                        }
                    });

            fermeture.setSite(null);
            fermeture.setGlobal(true);
        } else {
            SiteEntity site = siteRepository.findById(dto.siteId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Site introuvable avec l'id : " + dto.siteId()
                    ));

            jourFermetureRepository
                    .findBySiteIdAndDateFermeture(
                            dto.siteId(),
                            dto.dateFermeture()
                    )
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new ResponseStatusException(
                                    HttpStatus.CONFLICT,
                                    "Une autre fermeture existe déjà pour le site "
                                            + dto.siteId()
                                            + " à la date : "
                                            + dto.dateFermeture()
                            );
                        }
                    });

            fermeture.setSite(site);
            fermeture.setGlobal(false);
        }

        fermeture.setDateFermeture(dto.dateFermeture());
        fermeture.setRaison(dto.raison());

        return jourFermetureRepository.save(fermeture);
    }

    public void delete(Integer id) {
        if (!jourFermetureRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Jour de fermeture introuvable avec l'id : " + id
            );
        }

        jourFermetureRepository.deleteById(id);
    }

    private void validate(JourFermetureDTO dto) {
        if (dto == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le corps de la requête est obligatoire"
            );
        }

        if (dto.dateFermeture() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La date de fermeture est obligatoire"
            );
        }

        if (dto.global() && dto.siteId() != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Une fermeture globale ne doit pas être liée à un site"
            );
        }

        if (!dto.global() && dto.siteId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le site est obligatoire pour une fermeture non globale"
            );
        }

        if (dto.raison() == null || dto.raison().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La raison de fermeture est obligatoire"
            );
        }
    }
}