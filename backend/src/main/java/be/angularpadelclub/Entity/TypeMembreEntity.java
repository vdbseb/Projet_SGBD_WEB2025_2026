package be.angularpadelclub.Entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "type_membre")
public class TypeMembreEntity {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String code; // GLOBAL, SITE, LIBRE

    @Column(nullable = false)
    private int delai_reservation_jours;
}