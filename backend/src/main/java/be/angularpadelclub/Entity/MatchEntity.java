package be.angularpadelclub.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "match_padel")
public class MatchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "terrain_id", nullable = false)
    private CourtEntity terrain;

    @ManyToOne(optional = false)
    @JoinColumn(name = "organisateur_id", nullable = false)
    private MemberEntity organisateur;

    @Column(name = "date_match", nullable = false)
    private LocalDate dateMatch;

    @Column(name = "heure_debut", nullable = false)
    private LocalTime heureDebut;

    @Column(name = "heure_fin", nullable = false)
    private LocalTime heureFin;

    @Column(name = "type_match", nullable = false)
    private String typeMatch;

    @Column(name = "statut", nullable = false)
    private String statut;

    @Column(name = "prix_total", nullable = false)
    private BigDecimal prixTotal;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}