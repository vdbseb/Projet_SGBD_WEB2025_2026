package be.angularpadelclub.Entity;

import be.angularpadelclub.Enum.DetteRaison;
import be.angularpadelclub.Enum.DetteStatut;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "dette_membre")
public class DetteMembreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membre_id", nullable = false)
    private MembreEntity membre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participation_id")
    private ParticipationEntity participation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private ReservationEntity reservation;

    @Column(name = "montant_centimes", nullable = false)
    private Integer montantCentimes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DetteStatut statut = DetteStatut.OUVERTE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DetteRaison raison = DetteRaison.PARTICIPATION_IMPAYEE;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(name = "date_resolution")
    private LocalDateTime dateResolution;

    @PrePersist
    public void prePersist() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }

        if (statut == null) {
            statut = DetteStatut.OUVERTE;
        }

        if (raison == null) {
            raison = DetteRaison.PARTICIPATION_IMPAYEE;
        }
    }
}