package be.angularpadelclub.Service;

import be.angularpadelclub.Entity.AdministrateurEntity;
import be.angularpadelclub.Entity.CourtEntity;
import be.angularpadelclub.Entity.HoraireSiteEntity;
import be.angularpadelclub.Entity.JourFermetureEntity;
import be.angularpadelclub.Entity.MatchEntity;
import be.angularpadelclub.Entity.MembreEntity;
import be.angularpadelclub.Entity.PaiementEntity;
import be.angularpadelclub.Entity.ParticipationEntity;
import be.angularpadelclub.Entity.PenaliteEntity;
import be.angularpadelclub.Entity.ReservationEntity;
import be.angularpadelclub.Entity.SiteEntity;
import be.angularpadelclub.Repository.AdministrateurRepository;
import be.angularpadelclub.Repository.CourtRepository;
import be.angularpadelclub.Repository.HoraireSiteRepository;
import be.angularpadelclub.Repository.JourFermetureRepository;
import be.angularpadelclub.Repository.MatchRepository;
import be.angularpadelclub.Repository.MembreRepository;
import be.angularpadelclub.Repository.PaiementRepository;
import be.angularpadelclub.Repository.ParticipationRepository;
import be.angularpadelclub.Repository.PenaliteRepository;
import be.angularpadelclub.Repository.ReservationRepository;
import be.angularpadelclub.Repository.SiteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReferenceLookupService {

    private final AdministrateurRepository administrateurRepository;
    private final CourtRepository courtRepository;
    private final HoraireSiteRepository horaireSiteRepository;
    private final JourFermetureRepository jourFermetureRepository;
    private final MatchRepository matchRepository;
    private final MembreRepository membreRepository;
    private final PaiementRepository paiementRepository;
    private final ParticipationRepository participationRepository;
    private final PenaliteRepository penaliteRepository;
    private final ReservationRepository reservationRepository;
    private final SiteRepository siteRepository;

    public ReferenceLookupService(
            AdministrateurRepository administrateurRepository,
            CourtRepository courtRepository,
            HoraireSiteRepository horaireSiteRepository,
            JourFermetureRepository jourFermetureRepository,
            MatchRepository matchRepository,
            MembreRepository membreRepository,
            PaiementRepository paiementRepository,
            ParticipationRepository participationRepository,
            PenaliteRepository penaliteRepository,
            ReservationRepository reservationRepository,
            SiteRepository siteRepository
    ) {
        this.administrateurRepository = administrateurRepository;
        this.courtRepository = courtRepository;
        this.horaireSiteRepository = horaireSiteRepository;
        this.jourFermetureRepository = jourFermetureRepository;
        this.matchRepository = matchRepository;
        this.membreRepository = membreRepository;
        this.paiementRepository = paiementRepository;
        this.participationRepository = participationRepository;
        this.penaliteRepository = penaliteRepository;
        this.reservationRepository = reservationRepository;
        this.siteRepository = siteRepository;
    }

    public AdministrateurEntity findAdministrateurOrThrow(Integer id) {
        if (id == null) {
            throw badRequest("L'identifiant de l'administrateur est obligatoire.");
        }

        return administrateurRepository.findById(id)
                .orElseThrow(() -> notFound(
                        "Administrateur introuvable avec l'id : " + id
                ));
    }

    public AdministrateurEntity findAdministrateurByMatriculeOrThrow(
            String matricule
    ) {
        if (isBlank(matricule)) {
            throw badRequest("Le matricule de l'administrateur est obligatoire.");
        }

        return administrateurRepository.findByMatricule(matricule)
                .orElseThrow(() -> notFound(
                        "Administrateur introuvable avec le matricule : " + matricule
                ));
    }

    public CourtEntity findCourtOrThrow(Integer id) {
        if (id == null) {
            throw badRequest("L'identifiant du terrain est obligatoire.");
        }

        return courtRepository.findById(id)
                .orElseThrow(() -> notFound(
                        "Terrain introuvable avec l'id : " + id
                ));
    }

    public HoraireSiteEntity findHoraireSiteOrThrow(Integer id) {
        if (id == null) {
            throw badRequest("L'identifiant de l'horaire de site est obligatoire.");
        }

        return horaireSiteRepository.findById(id)
                .orElseThrow(() -> notFound(
                        "Horaire de site introuvable avec l'id : " + id
                ));
    }

    public JourFermetureEntity findJourFermetureOrThrow(Integer id) {
        if (id == null) {
            throw badRequest("L'identifiant du jour de fermeture est obligatoire.");
        }

        return jourFermetureRepository.findById(id)
                .orElseThrow(() -> notFound(
                        "Jour de fermeture introuvable avec l'id : " + id
                ));
    }

    public MatchEntity findMatchOrThrow(Integer id) {
        if (id == null) {
            throw badRequest("L'identifiant du match est obligatoire.");
        }

        return matchRepository.findById(id)
                .orElseThrow(() -> notFound(
                        "Match introuvable avec l'id : " + id
                ));
    }

    public MembreEntity findMembreOrThrow(Integer id) {
        if (id == null) {
            throw badRequest("L'identifiant du membre est obligatoire.");
        }

        return membreRepository.findById(id)
                .orElseThrow(() -> notFound(
                        "Membre introuvable avec l'id : " + id
                ));
    }

    public MembreEntity findMembreByMatriculeOrThrow(String matricule) {
        if (isBlank(matricule)) {
            throw badRequest("Le matricule du membre est obligatoire.");
        }

        return membreRepository.findByMatricule(matricule)
                .orElseThrow(() -> notFound(
                        "Membre introuvable avec le matricule : " + matricule
                ));
    }

    public PaiementEntity findPaiementOrThrow(Integer id) {
        if (id == null) {
            throw badRequest("L'identifiant du paiement est obligatoire.");
        }

        return paiementRepository.findById(id)
                .orElseThrow(() -> notFound(
                        "Paiement introuvable avec l'id : " + id
                ));
    }

    public ParticipationEntity findParticipationOrThrow(Integer id) {
        if (id == null) {
            throw badRequest("L'identifiant de la participation est obligatoire.");
        }

        return participationRepository.findById(id)
                .orElseThrow(() -> notFound(
                        "Participation introuvable avec l'id : " + id
                ));
    }

    public PenaliteEntity findPenaliteOrThrow(Integer id) {
        if (id == null) {
            throw badRequest("L'identifiant de la pénalité est obligatoire.");
        }

        return penaliteRepository.findById(id)
                .orElseThrow(() -> notFound(
                        "Pénalité introuvable avec l'id : " + id
                ));
    }

    public ReservationEntity findReservationOrThrow(Integer id) {
        if (id == null) {
            throw badRequest("L'identifiant de la réservation est obligatoire.");
        }

        return reservationRepository.findById(id)
                .orElseThrow(() -> notFound(
                        "Réservation introuvable avec l'id : " + id
                ));
    }

    public SiteEntity findSiteOrThrow(Integer id) {
        if (id == null) {
            throw badRequest("L'identifiant du site est obligatoire.");
        }

        return siteRepository.findById(id)
                .orElseThrow(() -> notFound(
                        "Site introuvable avec l'id : " + id
                ));
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                message
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}