package be.angularpadelclub.Service;

import be.angularpadelclub.DTO.MemberWalletDTO;
import be.angularpadelclub.DTO.PaiementDTO;
import be.angularpadelclub.Entity.DetteMembreEntity;
import be.angularpadelclub.Entity.MembreEntity;
import be.angularpadelclub.Entity.PaiementEntity;
import be.angularpadelclub.Entity.ParticipationEntity;
import be.angularpadelclub.Entity.ReservationEntity;
import be.angularpadelclub.Enum.*;
import be.angularpadelclub.Mapper.DetteMembreMapper;
import be.angularpadelclub.Mapper.PaiementMapper;
import be.angularpadelclub.Repository.DetteMembreRepository;
import be.angularpadelclub.Repository.MembreRepository;
import be.angularpadelclub.Repository.PaiementRepository;
import be.angularpadelclub.Repository.ParticipationRepository;
import be.angularpadelclub.Repository.ReservationRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaiementService {

    private static final int DEFAULT_PLAYER_SHARE_CENTS = 1500;

    private final PaiementRepository paiementRepository;
    private final ReservationRepository reservationRepository;
    private final ParticipationRepository participationRepository;
    private final DetteMembreRepository detteMembreRepository;
    private final MembreRepository membreRepository;
    private final PaiementMapper paiementMapper;
    private final DetteMembreMapper detteMembreMapper;

    public PaiementService(
            PaiementRepository paiementRepository,
            ReservationRepository reservationRepository,
            ParticipationRepository participationRepository,
            DetteMembreRepository detteMembreRepository,
            MembreRepository membreRepository,
            PaiementMapper paiementMapper,
            DetteMembreMapper detteMembreMapper
    ) {
        this.paiementRepository = paiementRepository;
        this.reservationRepository = reservationRepository;
        this.participationRepository = participationRepository;
        this.detteMembreRepository = detteMembreRepository;
        this.membreRepository = membreRepository;
        this.paiementMapper = paiementMapper;
        this.detteMembreMapper = detteMembreMapper;
    }

    public List<PaiementDTO> findAll() {
        return paiementMapper.toDTOList(paiementRepository.findAll());
    }

    public Optional<PaiementDTO> findById(Integer id) {
        return paiementRepository.findById(id)
                .map(paiementMapper::toDTO);
    }

    public List<PaiementDTO> findByReservation(Integer reservationId) {
        return paiementMapper.toDTOList(
                paiementRepository.findByReservation_Id(reservationId)
        );
    }

    public MemberWalletDTO getWallet(Integer memberId) {
        List<DetteMembreEntity> openDebts =
                detteMembreRepository.findByMembre_IdAndStatut(
                        memberId,
                        DetteStatut.OUVERTE
                );

        List<PaiementEntity> payments =
                paiementRepository.findByMembre_Id(memberId);

        MembreEntity member = membreRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Membre introuvable avec l'id " + memberId
                ));

        int due = openDebts.stream()
                .mapToInt(this::getMontantDetteCentimes)
                .sum();

        int pending = payments.stream()
                .filter(p -> p.getStatut() == PaiementStatut.EN_ATTENTE)
                .mapToInt(this::getMontantPaiementCentimes)
                .sum();

        int paid = payments.stream()
                .filter(p -> p.getStatut() == PaiementStatut.VALIDE)
                .mapToInt(this::getMontantPaiementCentimes)
                .sum();

        int refunded = payments.stream()
                .filter(p -> p.getStatut() == PaiementStatut.REMBOURSE)
                .mapToInt(this::getMontantPaiementCentimes)
                .sum();

        int balance = -due;

        return new MemberWalletDTO(
                member.getId(),
                member.getMatricule(),
                member.getPrenom() + " " + member.getNom(),
                "EUR",
                due,
                pending,
                paid,
                refunded,
                0,
                balance,
                detteMembreMapper.toDTOList(openDebts)
        );
    }

    @Transactional
    public PaiementDTO initierPaiement(Integer reservationId) {
        ReservationEntity reservation = findReservationOrThrow(reservationId);
        ParticipationEntity participation = findOrganizerParticipation(reservation)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Participation organisateur introuvable."
                ));

        return initierPaiementPourParticipation(participation.getId());
    }

    @Transactional
    public PaiementDTO initierPaiementPourParticipation(Integer participationId) {
        ParticipationEntity participation = findParticipationOrThrow(participationId);

        if (participation.getStatut() == ParticipationStatut.PAYEE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cette participation est deja payee."
            );
        }

        if (participation.getStatut() == ParticipationStatut.LIBEREE
                || participation.getStatut() == ParticipationStatut.ANNULEE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cette participation n'est plus payable."
            );
        }

        if (paiementRepository.existsByParticipation_IdAndStatut(
                participationId,
                PaiementStatut.EN_ATTENTE
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Un paiement est deja en attente pour cette participation."
            );
        }

        if (participation.getMembre() == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Participation sans membre associe."
            );
        }

        if (participation.getMatch() == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Participation sans match associe."
            );
        }

        MembreEntity member = participation.getMembre();
        int openDebtAmount = openDebtAmount(member.getId());
        int participationAmount = participation.getMontantDuCentimes() != null
                ? participation.getMontantDuCentimes()
                : DEFAULT_PLAYER_SHARE_CENTS;

        PaiementEntity paiement = new PaiementEntity();
        LocalDateTime now = LocalDateTime.now();

        paiement.setReservation(participation.getMatch().getReservation());
        paiement.setParticipation(participation);
        paiement.setMembre(member);
        paiement.setMontantCentimes(participationAmount + openDebtAmount);
        paiement.setDevise("EUR");
        paiement.setProvider(PaiementProvider.MOCK);
        paiement.setProviderPaymentId("mock_pi_part_" + participationId + "_" + now.getNano());
        paiement.setClientSecret("mock_secret_part_" + participationId + "_" + now.getNano());
        paiement.setMethode(PaiementMethode.CARTE);
        paiement.setStatut(PaiementStatut.EN_ATTENTE);
        paiement.setDateCreation(now);
        paiement.setDateExpiration(now.plusMinutes(15));

        return paiementMapper.toDTO(paiementRepository.save(paiement));
    }

    @Transactional
    public PaiementDTO confirmerPaiement(Integer paiementId) {
        PaiementEntity paiement = findPaiementOrThrow(paiementId);

        if (paiement.getStatut() == PaiementStatut.VALIDE) {
            return paiementMapper.toDTO(paiement);
        }

        if (paiement.getStatut() != PaiementStatut.EN_ATTENTE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Seul un paiement en attente peut etre confirme."
            );
        }

        paiement.setStatut(PaiementStatut.VALIDE);
        paiement.setDatePaiement(LocalDateTime.now());

        if (paiement.getParticipation() != null) {
            paiement.getParticipation().setStatut(ParticipationStatut.PAYEE);
        }

        if (paiement.getReservation() != null) {
            paiement.getReservation().setStatut(ReservationStatus.VALIDEE);
        }

        if (paiement.getMembre() != null) {
            closeOpenDebts(paiement.getMembre().getId());
        }

        return paiementMapper.toDTO(paiementRepository.save(paiement));
    }

    @Transactional
    public PaiementDTO refuserPaiement(Integer paiementId) {
        PaiementEntity paiement = findPaiementOrThrow(paiementId);

        if (paiement.getStatut() != PaiementStatut.EN_ATTENTE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Seul un paiement en attente peut etre refuse."
            );
        }

        paiement.setStatut(PaiementStatut.REFUSE);
        paiement.setDateExpiration(null);

        return paiementMapper.toDTO(paiementRepository.save(paiement));
    }

    @Transactional
    public PaiementDTO annulerPaiement(Integer paiementId) {
        PaiementEntity paiement = findPaiementOrThrow(paiementId);

        if (paiement.getStatut() != PaiementStatut.EN_ATTENTE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Seul un paiement en attente peut etre annule."
            );
        }

        paiement.setStatut(PaiementStatut.ANNULE);
        paiement.setDateExpiration(null);

        return paiementMapper.toDTO(paiementRepository.save(paiement));
    }

    @Transactional
    public PaiementDTO rembourserPaiement(Integer paiementId) {
        PaiementEntity paiement = findPaiementOrThrow(paiementId);

        if (paiement.getStatut() != PaiementStatut.VALIDE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Seul un paiement valide peut etre rembourse."
            );
        }

        paiement.setStatut(PaiementStatut.REMBOURSE);

        if (paiement.getParticipation() != null) {
            paiement.getParticipation().setStatut(ParticipationStatut.ANNULEE);
        }

        return paiementMapper.toDTO(paiementRepository.save(paiement));
    }

    @Transactional
    public void rembourserPaiementsReservation(ReservationEntity reservation) {
        if (reservation == null || reservation.getId() == null) {
            return;
        }

        List<PaiementEntity> paiements =
                paiementRepository.findByReservation_IdAndStatut(
                        reservation.getId(),
                        PaiementStatut.VALIDE
                );

        for (PaiementEntity paiement : paiements) {
            rembourserPaiementValide(paiement);
        }

        paiementRepository.saveAll(paiements);
    }

    @Transactional
    public void rembourserPaiementsMatch(Integer matchId) {
        if (matchId == null) {
            return;
        }

        List<PaiementEntity> paiements =
                paiementRepository.findByParticipation_Match_IdAndStatut(
                        matchId,
                        PaiementStatut.VALIDE
                );

        for (PaiementEntity paiement : paiements) {
            rembourserPaiementValide(paiement);
        }

        paiementRepository.saveAll(paiements);
    }

    @Transactional
    public void rembourserPaiementsParticipation(Integer participationId) {
        if (participationId == null) {
            return;
        }

        List<PaiementEntity> paiementsValides =
                paiementRepository.findByParticipation_IdAndStatut(
                        participationId,
                        PaiementStatut.VALIDE
                );

        for (PaiementEntity paiement : paiementsValides) {
            rembourserPaiementValide(paiement);
        }

        paiementRepository.saveAll(paiementsValides);

        List<PaiementEntity> paiementsEnAttente =
                paiementRepository.findByParticipation_IdAndStatut(
                        participationId,
                        PaiementStatut.EN_ATTENTE
                );

        for (PaiementEntity paiement : paiementsEnAttente) {
            paiement.setStatut(PaiementStatut.ANNULE);
            paiement.setDateExpiration(null);
        }

        paiementRepository.saveAll(paiementsEnAttente);
    }

    @Transactional
    public void createDebt(
            MembreEntity member,
            ParticipationEntity participation,
            ReservationEntity reservation,
            Integer amountCentimes,
            DetteRaison raison
    ) {
        if (member == null) {
            return;
        }

        if (participation != null
                && detteMembreRepository.existsByParticipation_IdAndStatut(
                participation.getId(),
                DetteStatut.OUVERTE
        )) {
            return;
        }

        DetteMembreEntity debt = new DetteMembreEntity();
        debt.setMembre(member);
        debt.setParticipation(participation);
        debt.setReservation(reservation);
        debt.setMontantCentimes(amountCentimes != null ? amountCentimes : 0);
        debt.setRaison(raison);
        debt.setStatut(DetteStatut.OUVERTE);
        debt.setDateCreation(LocalDateTime.now());

        detteMembreRepository.save(debt);
    }

    private void rembourserPaiementValide(PaiementEntity paiement) {
        if (paiement == null || paiement.getStatut() != PaiementStatut.VALIDE) {
            return;
        }

        paiement.setStatut(PaiementStatut.REMBOURSE);

        if (paiement.getParticipation() != null) {
            paiement.getParticipation().setStatut(ParticipationStatut.ANNULEE);
        }
    }

    private int openDebtAmount(Integer memberId) {
        return detteMembreRepository.findByMembre_IdAndStatut(
                        memberId,
                        DetteStatut.OUVERTE
                )
                .stream()
                .mapToInt(this::getMontantDetteCentimes)
                .sum();
    }

    private void closeOpenDebts(Integer memberId) {
        List<DetteMembreEntity> openDebts =
                detteMembreRepository.findByMembre_IdAndStatut(
                        memberId,
                        DetteStatut.OUVERTE
                );

        LocalDateTime now = LocalDateTime.now();

        for (DetteMembreEntity debt : openDebts) {
            debt.setStatut(DetteStatut.PAYEE);
            debt.setDateResolution(now);
        }

        detteMembreRepository.saveAll(openDebts);
    }

    private int getMontantDetteCentimes(DetteMembreEntity dette) {
        return dette.getMontantCentimes() != null
                ? dette.getMontantCentimes()
                : 0;
    }

    private int getMontantPaiementCentimes(PaiementEntity paiement) {
        return paiement.getMontantCentimes() != null
                ? paiement.getMontantCentimes()
                : 0;
    }

    private ReservationEntity findReservationOrThrow(Integer reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Reservation introuvable avec l'id " + reservationId
                ));
    }

    private ParticipationEntity findParticipationOrThrow(Integer participationId) {
        return participationRepository.findById(participationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Participation introuvable avec l'id " + participationId
                ));
    }

    private PaiementEntity findPaiementOrThrow(Integer paiementId) {
        return paiementRepository.findById(paiementId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Paiement introuvable avec l'id " + paiementId
                ));
    }

    private Optional<ParticipationEntity> findOrganizerParticipation(
            ReservationEntity reservation
    ) {
        if (reservation.getMatch() == null
                || reservation.getMatch().getOrganisateur() == null) {
            return Optional.empty();
        }

        return participationRepository.findByMatch_IdAndMembre_Id(
                reservation.getMatch().getId(),
                reservation.getMatch().getOrganisateur().getId()
        );
    }
}