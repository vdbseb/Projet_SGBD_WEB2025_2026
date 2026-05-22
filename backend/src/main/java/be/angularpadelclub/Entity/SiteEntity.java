package be.angularpadelclub.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "site")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SiteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nom;

    private String adresse;

    private String ville;

    @Column(name = "code_postal")
    private String codePostal;

    private Boolean actif;

    private String description;

    @Column(name = "image_url")
    private String imageUrl;
}