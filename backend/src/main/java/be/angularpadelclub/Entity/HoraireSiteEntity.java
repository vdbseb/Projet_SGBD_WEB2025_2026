package be.angularpadelclub.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "horaire_site",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_horaire_site_annee",
                        columnNames = {"site_id", "annee"}
                )
        }
)
public class HoraireSiteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private SiteEntity site;

    @Column(nullable = false)
    private int annee;

    @Column(nullable = false)
    private LocalTime heure_debut;

    @Column(nullable = false)
    private LocalTime heure_fin;

    @Column(nullable = false)
    private int duree_match_minutes;

    @Column(nullable = false)
    private int pause_minutes;
}