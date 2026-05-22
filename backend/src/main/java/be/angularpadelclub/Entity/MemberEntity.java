package be.angularpadelclub.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberEntity {

    @Id
    @Column(nullable = false, unique = true)
    private String matricule;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberType type;

    /**
     * Seulement pour les membres SITE
     * null pour GLOBAL et LIBRE
     */
    @ManyToOne
    @JoinColumn(name = "site_id")
    private SiteEntity site;

    /**
     * Empêche réservation si dette > 0
     */
    @Column(nullable = false)
    private Double soldeDu = 0.0;

    /**
     * Nombre de jours de pénalité de réservation
     */
    @Column(nullable = false)
    private Integer penaliteJours = 0;

    @Column(nullable = false)
    private Boolean actif = true;
}