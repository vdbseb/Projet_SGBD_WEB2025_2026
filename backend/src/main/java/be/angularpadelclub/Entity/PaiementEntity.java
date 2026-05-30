package be.angularpadelclub.Entity;

import be.angularpadelclub.Enum.PaiementMethode;
import be.angularpadelclub.Enum.PaiementProvider;
import be.angularpadelclub.Enum.PaiementStatut;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "paiement")
public class PaiementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private ReservationEntity reservation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membre_id", nullable = false)
    private MembreEntity membre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participation_match_id")
    private ParticipationEntity participation;

    @Column(name = "montant_centimes", nullable = false)
    private Integer montantCentimes;

    @Column(nullable = false, length = 3)
    private String devise = "EUR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaiementProvider provider = PaiementProvider.MOCK;

    @Column(name = "provider_payment_id", length = 150)
    private String providerPaymentId;

    @Column(name = "client_secret", length = 255)
    private String clientSecret;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(name = "date_paiement")
    private LocalDateTime datePaiement;

    @Column(name = "date_expiration")
    private LocalDateTime dateExpiration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaiementMethode methode = PaiementMethode.CARTE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaiementStatut statut = PaiementStatut.EN_ATTENTE;

    @PrePersist
    public void prePersist() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }

        if (devise == null || devise.isBlank()) {
            devise = "EUR";
        }

        if (provider == null) {
            provider = PaiementProvider.MOCK;
        }

        if (methode == null) {
            methode = PaiementMethode.CARTE;
        }

        if (statut == null) {
            statut = PaiementStatut.EN_ATTENTE;
        }
    }
}