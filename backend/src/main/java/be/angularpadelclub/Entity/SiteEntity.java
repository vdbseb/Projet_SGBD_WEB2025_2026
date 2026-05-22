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

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String adresse;

    @Column(nullable = false)
    private String ville;

    @Column (nullable = false)
    private String code_postal;

    @Column(nullable = false)
    private boolean actif;

    @Column(nullable = false)
    private String description;


    @Column(nullable = false)
    private String image_url;

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HoraireSiteEntity> horaire;
}