package be.angularpadelclub.Entity;

import be.angularpadelclub.Enum.PaiementMethode;
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
    @JoinColumn(name = "participation_match_id", nullable = false)
    private ParticipationEntity participation;

    @Column(nullable = false)
    private Double montant;

    @Column(name = "date_paiement", nullable = false)
    private LocalDateTime datePaiement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaiementMethode methode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaiementStatut statut;
}