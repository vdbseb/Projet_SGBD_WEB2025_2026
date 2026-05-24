package be.angularpadelclub.Entity;


import jakarta.persistence.*;
import lombok.*;



@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "membre")
public class MembreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private boolean actif;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, unique = true)
    private String matricule;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false)
    private String nom;

    @ManyToOne(optional = false)
    @JoinColumn(name = "type_membre_id", nullable = false)
    private TypeMembreEntity type;

    @ManyToOne
    @JoinColumn(name = "site_id")
    private SiteEntity site;
}