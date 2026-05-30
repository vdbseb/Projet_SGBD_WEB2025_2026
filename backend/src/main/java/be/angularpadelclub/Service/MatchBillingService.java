package be.angularpadelclub.Service;

import be.angularpadelclub.Entity.DetteMembreEntity;
import be.angularpadelclub.Entity.MatchEntity;
import be.angularpadelclub.Entity.ParticipationEntity;
import be.angularpadelclub.Enum.DetteRaison;
import be.angularpadelclub.Enum.DetteStatut;
import be.angularpadelclub.Enum.ParticipationStatut;
import be.angularpadelclub.Repository.DetteMembreRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MatchBillingService {

    private static final int DEFAULT_MATCH_PRICE_EUROS = 60;
    private static final int CENTS_PER_EURO = 100;

    private final DetteMembreRepository detteMembreRepository;

    public MatchBillingService(DetteMembreRepository detteMembreRepository) {
        this.detteMembreRepository = detteMembreRepository;
    }

    @Transactional
    public void facturerSoldeOrganisateurSiNecessaire(MatchEntity match) {

        if (match == null
                || match.getOrganisateur() == null
                || match.getReservation() == null) {
            return;
        }

        int soldeCentimes = calculerSoldeOrganisateurCentimes(match);

        if (soldeCentimes <= 0) {
            return;
        }

        boolean detteExistante =
                detteMembreRepository.existsByMembre_IdAndReservation_IdAndRaisonAndStatut(
                        match.getOrganisateur().getId(),
                        match.getReservation().getId(),
                        DetteRaison.SOLDE_ORGANISATEUR,
                        DetteStatut.OUVERTE
                );

        if (detteExistante) {
            return;
        }

        DetteMembreEntity dette = new DetteMembreEntity();
        dette.setMembre(match.getOrganisateur());
        dette.setReservation(match.getReservation());
        dette.setMontantCentimes(soldeCentimes);
        dette.setRaison(DetteRaison.SOLDE_ORGANISATEUR);
        dette.setStatut(DetteStatut.OUVERTE);
        dette.setDateCreation(LocalDateTime.now());

        detteMembreRepository.save(dette);
    }

    int calculerSoldeOrganisateurCentimes(MatchEntity match) {

        int prixTotalCentimes = getPrixTotalCentimes(match);
        int montantDejaCouvertCentimes = getMontantCouvertParParticipationsPayees(match);

        return Math.max(0, prixTotalCentimes - montantDejaCouvertCentimes);
    }

    private int getPrixTotalCentimes(MatchEntity match) {
        Integer prixTotalEuros = match.getPrixTotal() != null
                ? match.getPrixTotal()
                : DEFAULT_MATCH_PRICE_EUROS;

        return prixTotalEuros * CENTS_PER_EURO;
    }

    private int getMontantCouvertParParticipationsPayees(MatchEntity match) {

        if (match.getParticipations() == null) {
            return 0;
        }

        return match.getParticipations()
                .stream()
                .filter(this::estParticipationPayee)
                .map(ParticipationEntity::getMontantDuCentimes)
                .mapToInt(montant -> montant != null ? montant : 0)
                .sum();
    }

    private boolean estParticipationPayee(ParticipationEntity participation) {
        return participation != null
                && participation.getStatut() == ParticipationStatut.PAYEE;
    }
}