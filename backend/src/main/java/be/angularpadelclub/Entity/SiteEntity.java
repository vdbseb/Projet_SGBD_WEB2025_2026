package be.angularpadelclub.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "site")
public class SiteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(nullable = false, length = 255)
    private String adresse;

    @Column(nullable = false, length = 100)
    private String ville;

    @Column(name = "code_postal", nullable = false, length = 20)
    private String code_postal;

    @Column(nullable = false)
    private boolean actif = true;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false, length = 500)
    private String image_url;

    @OneToMany(
            mappedBy = "site",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<HoraireSiteEntity> horaire;

    @OneToMany(
            mappedBy = "site",
            fetch = FetchType.LAZY
    )
    private List<CourtEntity> courts;
}