package be.angularpadelclub.Entity;

import be.angularpadelclub.Enum.ParticipationStatut;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "participation",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_participation_match_membre",
                        columnNames = {"match_id", "membre_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private MatchEntity match;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membre_id", nullable = false)
    private MembreEntity membre;

    @Column(name = "date_inscription", nullable = false)
    private LocalDateTime dateInscription = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ParticipationStatut statut = ParticipationStatut.EN_ATTENTE_PAIEMENT;

    @Column(name = "montant_du_centimes", nullable = false)
    private Integer montantDuCentimes = 1500;

    @Column(name = "date_limite_paiement")
    private LocalDateTime dateLimitePaiement;

    @PrePersist
    public void prePersist() {
        if (dateInscription == null) {
            dateInscription = LocalDateTime.now();
        }

        if (statut == null) {
            statut = ParticipationStatut.EN_ATTENTE_PAIEMENT;
        }

        if (montantDuCentimes == null) {
            montantDuCentimes = 1500;
        }
    }
}