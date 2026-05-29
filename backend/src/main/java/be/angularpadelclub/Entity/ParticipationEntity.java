package be.angularpadelclub.Entity;


import be.angularpadelclub.Enum.ParticipationStatut;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "participation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private MatchEntity match;

    @ManyToOne(optional = false)
    @JoinColumn(name = "membre_id", nullable = false)
    private MembreEntity membre;

    @Column(name = "date_inscription")
    private LocalDateTime dateInscription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipationStatut statut = ParticipationStatut.EN_ATTENTE_PAIEMENT;

    @Column(name = "montant_du_centimes", nullable = false)
    private Integer montantDuCentimes = 1500;

    @Column(name = "date_limite_paiement")
    private LocalDateTime dateLimitePaiement;
}
