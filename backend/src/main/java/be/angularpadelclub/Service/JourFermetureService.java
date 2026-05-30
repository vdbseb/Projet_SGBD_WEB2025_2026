package be.angularpadelclub.Service;

import be.angularpadelclub.DTO.JourFermetureDTO;
import be.angularpadelclub.Entity.JourFermetureEntity;
import be.angularpadelclub.Entity.ReservationEntity;
import be.angularpadelclub.Entity.SiteEntity;
import be.angularpadelclub.Repository.JourFermetureRepository;
import be.angularpadelclub.Repository.ReservationRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class JourFermetureService {

    private final JourFermetureRepository jourFermetureRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationCancellationService reservationCancellationService;
    private final ReferenceLookupService referenceLookupService;

    public JourFermetureService(
            JourFermetureRepository jourFermetureRepository,
            ReservationRepository reservationRepository,
            ReservationCancellationService reservationCancellationService,
            ReferenceLookupService referenceLookupService
    ) {
        this.jourFermetureRepository = jourFermetureRepository;
        this.reservationRepository = reservationRepository;
        this.reservationCancellationService = reservationCancellationService;
        this.referenceLookupService = referenceLookupService;
    }

    public List<JourFermetureEntity> findAll() {
        return jourFermetureRepository.findAll();
    }

    public JourFermetureEntity findById(Integer id) {
        return referenceLookupService.findJourFermetureOrThrow(id);
    }

    public List<JourFermetureEntity> findBySiteId(Integer siteId) {
        referenceLookupService.findSiteOrThrow(siteId);

        return jourFermetureRepository.findBySiteId(siteId);
    }

    public List<JourFermetureEntity> findGlobalClosures() {
        return jourFermetureRepository.findByGlobalTrue();
    }

    @Transactional
    public JourFermetureEntity create(JourFermetureDTO dto) {
        validate(dto);

        JourFermetureEntity fermeture = dto.global()
                ? buildGlobalClosure(dto)
                : buildSiteClosure(dto);

        JourFermetureEntity saved = jourFermetureRepository.save(fermeture);
        cancelReservationsImpactedByClosure(saved);

        return saved;
    }

    @Transactional
    public JourFermetureEntity update(
            Integer id,
            JourFermetureDTO dto
    ) {
        validate(dto);

        JourFermetureEntity fermeture =
                referenceLookupService.findJourFermetureOrThrow(id);

        if (dto.global()) {
            validateNoDuplicateGlobalClosure(id, dto.dateFermeture());

            fermeture.setSite(null);
            fermeture.setGlobal(true);
        } else {
            SiteEntity site = referenceLookupService.findSiteOrThrow(dto.siteId());

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

    @Transactional
    public void delete(Integer id) {
        JourFermetureEntity fermeture =
                referenceLookupService.findJourFermetureOrThrow(id);

        jourFermetureRepository.delete(fermeture);
    }

    public boolean existsBySiteAndDate(
            Integer siteId,
            LocalDate date
    ) {
        if (siteId == null || date == null) {
            return false;
        }

        return jourFermetureRepository
                .existsBySiteIdAndDateFermeture(siteId, date);
    }

    public boolean existsGlobalByDate(
            LocalDate date
    ) {
        if (date == null) {
            return false;
        }

        return jourFermetureRepository
                .existsByGlobalTrueAndDateFermeture(date);
    }

    private JourFermetureEntity buildGlobalClosure(
            JourFermetureDTO dto
    ) {
        if (jourFermetureRepository.existsByGlobalTrueAndDateFermeture(
                dto.dateFermeture()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Une fermeture globale existe déjà pour la date : "
                            + dto.dateFermeture() + "."
            );
        }

        JourFermetureEntity fermeture = new JourFermetureEntity();

        fermeture.setSite(null);
        fermeture.setDateFermeture(dto.dateFermeture());
        fermeture.setRaison(dto.raison());
        fermeture.setGlobal(true);

        return fermeture;
    }

    private JourFermetureEntity buildSiteClosure(
            JourFermetureDTO dto
    ) {
        SiteEntity site = referenceLookupService.findSiteOrThrow(dto.siteId());

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
                            + "."
            );
        }

        JourFermetureEntity fermeture = new JourFermetureEntity();

        fermeture.setSite(site);
        fermeture.setDateFermeture(dto.dateFermeture());
        fermeture.setRaison(dto.raison());
        fermeture.setGlobal(false);

        return fermeture;
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
                                        + dateFermeture + "."
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
                                        + "."
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

        List<ReservationEntity> impactedReservations;

        if (fermeture.isGlobal()) {
            impactedReservations = reservationRepository.findByDate(
                    fermeture.getDateFermeture()
            );
        } else {
            if (fermeture.getSite() == null) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Fermeture de site invalide : aucun site n'est associé."
                );
            }

            impactedReservations = reservationRepository.findByDateAndCourt_Site_Id(
                    fermeture.getDateFermeture(),
                    fermeture.getSite().getId()
            );
        }

        reservationCancellationService.cancelReservationsForClubReason(
                impactedReservations
        );
    }

    private void validate(JourFermetureDTO dto) {
        if (dto == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le corps de la requête est obligatoire."
            );
        }

        if (dto.dateFermeture() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La date de fermeture est obligatoire."
            );
        }

        if (dto.global() && dto.siteId() != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Une fermeture globale ne doit pas être liée à un site."
            );
        }

        if (!dto.global() && dto.siteId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le site est obligatoire pour une fermeture non globale."
            );
        }

        if (dto.raison() == null || dto.raison().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La raison de fermeture est obligatoire."
            );
        }
    }
}