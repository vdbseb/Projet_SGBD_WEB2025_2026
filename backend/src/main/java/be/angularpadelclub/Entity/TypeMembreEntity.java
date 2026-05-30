package be.angularpadelclub.Entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "type_membre",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_type_membre_code",
                        columnNames = "code"
                )
        }
)
public class TypeMembreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 20)
    private String code; // GLOBAL, SITE, LIBRE

    @Column(nullable = false)
    private int delai_reservation_jours;
}