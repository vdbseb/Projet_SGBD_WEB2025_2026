package be.angularpadelclub.Entity;

import be.angularpadelclub.Enum.MatchStatus;
import be.angularpadelclub.Enum.MatchType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

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
    private MembreEntity organisateur;

    @Column(name = "date_match", nullable = false)
    private LocalDate dateMatch;

    @Column(name = "heure_debut", nullable = false)
    private LocalTime heureDebut;

    @Column(name = "heure_fin", nullable = false)
    private LocalTime heureFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_match", nullable = false)
    private MatchType typeMatch;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private MatchStatus statut;

    @Column(name = "prix_total", nullable = false)
    private Integer prixTotal;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL)
    private List<ParticipationEntity> participations;
}