package be.angularpadelclub.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "jour_fermeture",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_jour_fermeture_site_date",
                        columnNames = {"site_id", "date_fermeture"}
                )
        }
)
public class JourFermetureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    private SiteEntity site;

    @Column(name = "date_fermeture", nullable = false)
    private LocalDate dateFermeture;

    @Column(length = 255)
    private String raison;

    @Column(nullable = false)
    private boolean global;
}