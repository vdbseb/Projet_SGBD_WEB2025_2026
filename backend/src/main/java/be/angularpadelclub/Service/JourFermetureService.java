package be.angularpadelclub.Service;

import be.angularpadelclub.DTO.JourFermetureDTO;
import be.angularpadelclub.Entity.JourFermetureEntity;
import be.angularpadelclub.Entity.ReservationEntity;
import be.angularpadelclub.Entity.SiteEntity;
import be.angularpadelclub.Repository.JourFermetureRepository;
import be.angularpadelclub.Repository.ReservationRepository;
import be.angularpadelclub.Repository.SiteRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class JourFermetureService {

    private final JourFermetureRepository jourFermetureRepository;
    private final SiteRepository siteRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationCancellationService reservationCancellationService;

    public JourFermetureService(
            JourFermetureRepository jourFermetureRepository,
            SiteRepository siteRepository,
            ReservationRepository reservationRepository,
            ReservationCancellationService reservationCancellationService
    ) {
        this.jourFermetureRepository = jourFermetureRepository;
        this.siteRepository = siteRepository;
        this.reservationRepository = reservationRepository;
        this.reservationCancellationService = reservationCancellationService;
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

    @Transactional
    public JourFermetureEntity create(JourFermetureDTO dto) {
        validate(dto);

        if (dto.global()) {
            return createGlobalClosure(dto);
        }

        return createSiteClosure(dto);
    }

    @Transactional
    public JourFermetureEntity update(Integer id, JourFermetureDTO dto) {
        validate(dto);

        JourFermetureEntity fermeture = findById(id);

        if (dto.global()) {
            validateNoDuplicateGlobalClosure(id, dto.dateFermeture());

            fermeture.setSite(null);
            fermeture.setGlobal(true);
        } else {
            SiteEntity site = findSiteOrThrow(dto.siteId());
            validateNoDuplicateSiteClosure(
                    id,
                    dto.siteId(),
                    dto.dateFermeture()
            );

            fermeture.setSite(site);
            fermeture.setGlobal(false);
        }

        fermeture.setDateFermeture(dto.dateFermeture());
        fermeture.setRaison(dto.raison());

        JourFermetureEntity saved = jourFermetureRepository.save(fermeture);
        cancelReservationsImpactedByClosure(saved);

        return saved;
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

    public boolean existsBySiteAndDate(
            Integer siteId,
            LocalDate date
    ) {
        return jourFermetureRepository
                .existsBySiteIdAndDateFermeture(siteId, date);
    }

    public boolean existsGlobalByDate(
            LocalDate date
    ) {
        return jourFermetureRepository
                .existsByGlobalTrueAndDateFermeture(date);
    }

    private JourFermetureEntity createGlobalClosure(JourFermetureDTO dto) {
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

        JourFermetureEntity saved = jourFermetureRepository.save(fermeture);
        cancelReservationsImpactedByClosure(saved);

        return saved;
    }

    private JourFermetureEntity createSiteClosure(JourFermetureDTO dto) {
        SiteEntity site = findSiteOrThrow(dto.siteId());

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

        JourFermetureEntity saved = jourFermetureRepository.save(fermeture);
        cancelReservationsImpactedByClosure(saved);

        return saved;
    }

    private void validateNoDuplicateGlobalClosure(
            Integer currentClosureId,
            LocalDate dateFermeture
    ) {
        jourFermetureRepository
                .findByGlobalTrueAndDateFermeture(dateFermeture)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(currentClosureId)) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Une autre fermeture globale existe déjà pour la date : "
                                        + dateFermeture
                        );
                    }
                });
    }

    private void validateNoDuplicateSiteClosure(
            Integer currentClosureId,
            Integer siteId,
            LocalDate dateFermeture
    ) {
        jourFermetureRepository
                .findBySiteIdAndDateFermeture(
                        siteId,
                        dateFermeture
                )
                .ifPresent(existing -> {
                    if (!existing.getId().equals(currentClosureId)) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Une autre fermeture existe déjà pour le site "
                                        + siteId
                                        + " à la date : "
                                        + dateFermeture
                        );
                    }
                });
    }

    private void cancelReservationsImpactedByClosure(
            JourFermetureEntity fermeture
    ) {
        if (fermeture == null || fermeture.getDateFermeture() == null) {
            return;
        }

        List<ReservationEntity> impactedReservations =
                fermeture.isGlobal()
                        ? reservationRepository.findByDate(
                        fermeture.getDateFermeture()
                )
                        : reservationRepository.findByDateAndCourt_Site_Id(
                        fermeture.getDateFermeture(),
                        fermeture.getSite().getId()
                );

        reservationCancellationService.cancelReservationsForClubReason(
                impactedReservations
        );
    }

    private SiteEntity findSiteOrThrow(Integer siteId) {
        return siteRepository.findById(siteId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Site introuvable avec l'id : " + siteId
                ));
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