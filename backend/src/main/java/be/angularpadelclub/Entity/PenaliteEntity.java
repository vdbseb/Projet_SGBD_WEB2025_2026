package be.angularpadelclub.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "penalite")
public class PenaliteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "membre_id", nullable = false)
    private MembreEntity membre;

    @ManyToOne
    @JoinColumn(name = "match_id")
    private MatchEntity match;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Column(nullable = false)
    private String raison;

    @Column(nullable = false)
    private boolean active;
}