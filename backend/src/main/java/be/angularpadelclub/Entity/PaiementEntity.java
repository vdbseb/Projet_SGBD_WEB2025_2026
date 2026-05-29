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

    @ManyToOne(optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private ReservationEntity reservation;

    @ManyToOne(optional = false)
    @JoinColumn(name = "membre_id", nullable = false)
    private MembreEntity membre;

    @ManyToOne
    @JoinColumn(name = "participation_match_id")
    private ParticipationEntity participation;

    @Column(name = "montant_centimes", nullable = false)
    private Integer montantCentimes;

    @Column(nullable = false, length = 3)
    private String devise = "EUR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaiementProvider provider = PaiementProvider.MOCK;

    @Column(name = "provider_payment_id")
    private String providerPaymentId;

    @Column(name = "client_secret")
    private String clientSecret;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(name = "date_paiement")
    private LocalDateTime datePaiement;

    @Column(name = "date_expiration")
    private LocalDateTime dateExpiration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaiementMethode methode = PaiementMethode.CARTE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaiementStatut statut = PaiementStatut.EN_ATTENTE;
}
