package be.angularpadelclub.Service;

import be.angularpadelclub.DTO.PenaliteDTO;
import be.angularpadelclub.Entity.PenaliteEntity;
import be.angularpadelclub.Repository.MembreRepository;
import be.angularpadelclub.Repository.PenaliteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class PenaliteService {

    private final PenaliteRepository penaliteRepository;
    private final MembreRepository membreRepository;

    public PenaliteService(
            PenaliteRepository penaliteRepository,
            MembreRepository membreRepository
    ) {
        this.penaliteRepository = penaliteRepository;
        this.membreRepository = membreRepository;
    }

    public List<PenaliteDTO> findActivePenaltiesForMember(Integer memberId) {
        if (memberId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "L'identifiant du membre est obligatoire."
            );
        }

        if (!membreRepository.existsById(memberId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Membre introuvable avec l'id " + memberId
            );
        }

        LocalDate today = LocalDate.now();

        return penaliteRepository
                .findByMembre_IdAndActiveTrueAndDateFinGreaterThanEqualOrderByDateFinAsc(
                        memberId,
                        today
                )
                .stream()
                .map(penalite -> toDTO(penalite, today))
                .toList();
    }

    private PenaliteDTO toDTO(PenaliteEntity penalite, LocalDate today) {
        long joursRestants = ChronoUnit.DAYS.between(today, penalite.getDateFin());

        var match = penalite.getMatch();
        var reservation = match != null ? match.getReservation() : null;
        var court = reservation != null ? reservation.getCourt() : null;
        var site = court != null ? court.getSite() : null;

        return new PenaliteDTO(
                penalite.getId(),
                penalite.getMembre() != null ? penalite.getMembre().getId() : null,
                match != null ? match.getId() : null,
                penalite.getDateDebut(),
                penalite.getDateFin(),
                penalite.getRaison(),
                penalite.isActive(),
                Math.max(0, joursRestants),
                match != null ? match.getDateMatch() : null,
                match != null ? match.getHeureDebut() : null,
                match != null ? match.getHeureFin() : null,
                court != null ? court.getNom() : null,
                site != null ? site.getNom() : null
        );
    }
}