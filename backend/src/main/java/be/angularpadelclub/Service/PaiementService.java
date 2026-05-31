package be.angularpadelclub.Service;

import be.angularpadelclub.DTO.MemberWalletDTO;
import be.angularpadelclub.DTO.PaiementDTO;
import be.angularpadelclub.Entity.DetteMembreEntity;
import be.angularpadelclub.Entity.MembreEntity;
import be.angularpadelclub.Entity.PaiementEntity;
import be.angularpadelclub.Entity.ParticipationEntity;
import be.angularpadelclub.Entity.ReservationEntity;
import be.angularpadelclub.Enum.DetteRaison;
import be.angularpadelclub.Enum.DetteStatut;
import be.angularpadelclub.Enum.PaiementMethode;
import be.angularpadelclub.Enum.PaiementProvider;
import be.angularpadelclub.Enum.PaiementStatut;
import be.angularpadelclub.Enum.ParticipationStatut;
import be.angularpadelclub.Enum.ReservationStatus;
import be.angularpadelclub.Mapper.DetteMembreMapper;
import be.angularpadelclub.Mapper.PaiementMapper;
import be.angularpadelclub.Repository.DetteMembreRepository;
import be.angularpadelclub.Repository.PaiementRepository;
import be.angularpadelclub.Repository.ParticipationRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaiementService {

    private static final String DEFAULT_CURRENCY = "EUR";

    private final PaiementRepository paiementRepository;
    private final ParticipationRepository participationRepository;
    private final DetteMembreRepository detteMembreRepository;
    private final PaiementMapper paiementMapper;
    private final DetteMembreMapper detteMembreMapper;
    private final ReferenceLookupService referenceLookupService;

    public PaiementService(
            PaiementRepository paiementRepository,
            ParticipationRepository participationRepository,
            DetteMembreRepository detteMembreRepository,
            PaiementMapper paiementMapper,
            DetteMembreMapper detteMembreMapper,
            ReferenceLookupService referenceLookupService
    ) {
        this.paiementRepository = paiementRepository;
        this.participationRepository = participationRepository;
        this.detteMembreRepository = detteMembreRepository;
        this.paiementMapper = paiementMapper;
        this.detteMembreMapper = detteMembreMapper;
        this.referenceLookupService = referenceLookupService;
    }

    public List<PaiementDTO> findAll() {
        return paiementMapper.toDTOList(
                paiementRepository.findAll()
        );
    }

    public Optional<PaiementDTO> findById(Integer id) {
        return paiementRepository.findById(id)
                .map(paiementMapper::toDTO);
    }

    public PaiementDTO findByIdOrThrow(Integer id) {
        return paiementMapper.toDTO(
                referenceLookupService.findPaiementOrThrow(id)
        );
    }

    public List<PaiementDTO> findByReservation(Integer reservationId) {
        referenceLookupService.findReservationOrThrow(reservationId);

        return paiementMapper.toDTOList(
                paiementRepository.findByReservation_Id(reservationId)
        );
    }

    public MemberWalletDTO getWallet(Integer memberId) {
        MembreEntity member = referenceLookupService.findMembreOrThrow(memberId);

        List<DetteMembreEntity> openDebts =
                detteMembreRepository.findByMembre_IdAndStatut(
                        memberId,
                        DetteStatut.OUVERTE
                );

        List<PaiementEntity> payments =
                paiementRepository.findByMembre_Id(memberId);

        int due = openDebts.stream()
                .mapToInt(this::getMontantDetteCentimes)
                .sum();

        int pending = payments.stream()
                .filter(paiement -> paiement.getStatut() == PaiementStatut.EN_ATTENTE)
                .mapToInt(this::getMontantPaiementCentimes)
                .sum();

        int paid = payments.stream()
                .filter(paiement -> paiement.getStatut() == PaiementStatut.VALIDE)
                .mapToInt(this::getMontantPaiementCentimes)
                .sum();

        int refunded = payments.stream()
                .filter(paiement -> paiement.getStatut() == PaiementStatut.REMBOURSE)
                .mapToInt(this::getMontantPaiementCentimes)
                .sum();

        int balance = -due;

        return new MemberWalletDTO(
                member.getId(),
                member.getMatricule(),
                member.getPrenom() + " " + member.getNom(),
                DEFAULT_CURRENCY,
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
        ReservationEntity reservation =
                referenceLookupService.findReservationOrThrow(reservationId);

        ParticipationEntity participation = findOrganizerParticipation(reservation)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Participation organisateur introuvable."
                ));

        return initierPaiementPourParticipation(participation.getId());
    }

    @Transactional
    public PaiementDTO initierPaiementPourParticipation(Integer participationId) {
        ParticipationEntity participation =
                referenceLookupService.findParticipationOrThrow(participationId);

        validateParticipationPayable(participationId, participation);

        int amountCentimes = participation.getMontantDuCentimes() != null
                ? participation.getMontantDuCentimes()
                : ClubBusinessRules.DEFAULT_PLAYER_SHARE_CENTS;

        PaiementEntity paiement = buildMockPaiement(
                participation.getMembre(),
                participation.getMatch().getReservation(),
                participation,
                amountCentimes,
                "part_" + participationId
        );

        PaiementEntity saved = paiementRepository.saveAndFlush(paiement);

        ensurePaiementHasId(
                saved,
                "Le paiement de participation a été créé sans identifiant."
        );

        return paiementMapper.toDTO(saved);
    }

    @Transactional
    public PaiementDTO initierPaiementDettesMembre(Integer memberId) {
        MembreEntity member = referenceLookupService.findMembreOrThrow(memberId);

        cancelOldPendingDebtPayments(memberId);

        List<DetteMembreEntity> openDebts =
                detteMembreRepository.findByMembre_IdAndStatut(
                        memberId,
                        DetteStatut.OUVERTE
                );

        if (openDebts.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Aucune dette ouverte à payer pour ce membre."
            );
        }

        int totalDebtAmount = openDebts.stream()
                .mapToInt(this::getMontantDetteCentimes)
                .sum();

        if (totalDebtAmount <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Le montant total des dettes ouvertes doit être positif."
            );
        }

        PaiementEntity paiement = buildMockPaiement(
                member,
                null,
                null,
                totalDebtAmount,
                "debts_" + memberId
        );

        PaiementEntity saved = paiementRepository.saveAndFlush(paiement);

        ensurePaiementHasId(
                saved,
                "Le paiement de dettes a été créé sans identifiant."
        );

        PaiementEntity reloaded =
                referenceLookupService.findPaiementOrThrow(saved.getId());

        return paiementMapper.toDTO(reloaded);
    }

    @Transactional
    public PaiementDTO confirmerPaiement(Integer paiementId) {
        PaiementEntity paiement =
                referenceLookupService.findPaiementOrThrow(paiementId);

        if (paiement.getStatut() == PaiementStatut.VALIDE) {
            return paiementMapper.toDTO(paiement);
        }

        if (paiement.getStatut() != PaiementStatut.EN_ATTENTE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Seul un paiement en attente peut être confirmé. Statut actuel : "
                            + paiement.getStatut() + "."
            );
        }

        paiement.setStatut(PaiementStatut.VALIDE);
        paiement.setDatePaiement(LocalDateTime.now());
        paiement.setDateExpiration(null);

        if (paiement.getParticipation() != null) {
            paiement.getParticipation().setStatut(ParticipationStatut.PAYEE);
        }

        if (isPaiementParticipation(paiement) && paiement.getReservation() != null) {
            paiement.getReservation().setStatut(ReservationStatus.VALIDEE);
        }

        if (isPaiementDettes(paiement)) {
            if (paiement.getMembre() == null) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Paiement de dettes invalide : aucun membre associé."
                );
            }

            closeOpenDebts(paiement.getMembre().getId());
        }

        return paiementMapper.toDTO(
                paiementRepository.saveAndFlush(paiement)
        );
    }

    @Transactional
    public PaiementDTO refuserPaiement(Integer paiementId) {
        PaiementEntity paiement =
                referenceLookupService.findPaiementOrThrow(paiementId);

        if (paiement.getStatut() != PaiementStatut.EN_ATTENTE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Seul un paiement en attente peut être refusé. Statut actuel : "
                            + paiement.getStatut() + "."
            );
        }

        paiement.setStatut(PaiementStatut.REFUSE);
        paiement.setDateExpiration(null);

        return paiementMapper.toDTO(
                paiementRepository.saveAndFlush(paiement)
        );
    }

    @Transactional
    public PaiementDTO annulerPaiement(Integer paiementId) {
        PaiementEntity paiement =
                referenceLookupService.findPaiementOrThrow(paiementId);

        if (paiement.getStatut() != PaiementStatut.EN_ATTENTE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Seul un paiement en attente peut être annulé. Statut actuel : "
                            + paiement.getStatut() + "."
            );
        }

        paiement.setStatut(PaiementStatut.ANNULE);
        paiement.setDateExpiration(null);

        return paiementMapper.toDTO(
                paiementRepository.saveAndFlush(paiement)
        );
    }

    @Transactional
    public PaiementDTO rembourserPaiement(Integer paiementId) {
        PaiementEntity paiement =
                referenceLookupService.findPaiementOrThrow(paiementId);

        if (paiement.getStatut() != PaiementStatut.VALIDE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Seul un paiement valide peut être remboursé. Statut actuel : "
                            + paiement.getStatut() + "."
            );
        }

        if (isPaiementDettes(paiement)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Un paiement de dettes déjà validé ne peut pas être remboursé automatiquement."
            );
        }

        rembourserPaiementValide(paiement);

        return paiementMapper.toDTO(
                paiementRepository.saveAndFlush(paiement)
        );
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

        paiementRepository.saveAllAndFlush(paiements);
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

        paiementRepository.saveAllAndFlush(paiements);
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

        paiementRepository.saveAllAndFlush(paiementsValides);

        List<PaiementEntity> paiementsEnAttente =
                paiementRepository.findByParticipation_IdAndStatut(
                        participationId,
                        PaiementStatut.EN_ATTENTE
                );

        for (PaiementEntity paiement : paiementsEnAttente) {
            paiement.setStatut(PaiementStatut.ANNULE);
            paiement.setDateExpiration(null);
        }

        paiementRepository.saveAllAndFlush(paiementsEnAttente);
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
                && participation.getId() != null
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
        debt.setRaison(raison != null ? raison : DetteRaison.AJUSTEMENT);
        debt.setStatut(DetteStatut.OUVERTE);
        debt.setDateCreation(LocalDateTime.now());

        detteMembreRepository.save(debt);
    }

    private PaiementEntity buildMockPaiement(
            MembreEntity membre,
            ReservationEntity reservation,
            ParticipationEntity participation,
            Integer montantCentimes,
            String providerSuffix
    ) {
        LocalDateTime now = LocalDateTime.now();
        long uniqueSuffix = System.currentTimeMillis();

        PaiementEntity paiement = new PaiementEntity();

        paiement.setReservation(reservation);
        paiement.setParticipation(participation);
        paiement.setMembre(membre);
        paiement.setMontantCentimes(montantCentimes);
        paiement.setDevise(DEFAULT_CURRENCY);
        paiement.setProvider(PaiementProvider.MOCK);
        paiement.setProviderPaymentId("mock_pi_" + providerSuffix + "_" + uniqueSuffix);
        paiement.setClientSecret("mock_secret_" + providerSuffix + "_" + uniqueSuffix);
        paiement.setMethode(PaiementMethode.CARTE);
        paiement.setStatut(PaiementStatut.EN_ATTENTE);
        paiement.setDateCreation(now);
        paiement.setDateExpiration(now.plusMinutes(ClubBusinessRules.PAYMENT_EXPIRATION_MINUTES));

        return paiement;
    }

    private void ensurePaiementHasId(
            PaiementEntity paiement,
            String errorMessage
    ) {
        if (paiement == null || paiement.getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    errorMessage
            );
        }
    }

    private void cancelOldPendingDebtPayments(Integer memberId) {
        List<PaiementEntity> oldPendingDebtPayments =
                paiementRepository.findByMembre_IdAndParticipationIsNullAndReservationIsNullAndStatut(
                        memberId,
                        PaiementStatut.EN_ATTENTE
                );

        if (oldPendingDebtPayments.isEmpty()) {
            return;
        }

        for (PaiementEntity oldPayment : oldPendingDebtPayments) {
            oldPayment.setStatut(PaiementStatut.ANNULE);
            oldPayment.setDateExpiration(null);
        }

        paiementRepository.saveAllAndFlush(oldPendingDebtPayments);
    }

    private void validateParticipationPayable(
            Integer participationId,
            ParticipationEntity participation
    ) {
        if (participation.getStatut() == ParticipationStatut.PAYEE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cette participation est déjà payée."
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
                    "Un paiement est déjà en attente pour cette participation."
            );
        }

        if (participation.getMembre() == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Participation sans membre associé."
            );
        }

        if (participation.getMatch() == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Participation sans match associé."
            );
        }

        if (participation.getMatch().getReservation() == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Participation sans réservation associée."
            );
        }
    }

    private void rembourserPaiementValide(PaiementEntity paiement) {
        if (paiement == null || paiement.getStatut() != PaiementStatut.VALIDE) {
            return;
        }

        if (isPaiementDettes(paiement)) {
            return;
        }

        paiement.setStatut(PaiementStatut.REMBOURSE);

        if (paiement.getParticipation() != null) {
            paiement.getParticipation().setStatut(ParticipationStatut.ANNULEE);
        }
    }

    private boolean isPaiementParticipation(PaiementEntity paiement) {
        return paiement != null
                && paiement.getParticipation() != null;
    }

    private boolean isPaiementDettes(PaiementEntity paiement) {
        return paiement != null
                && paiement.getMembre() != null
                && paiement.getParticipation() == null
                && paiement.getReservation() == null;
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