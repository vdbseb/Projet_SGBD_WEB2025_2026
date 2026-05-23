package be.angularpadelclub.Entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "participation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private MatchEntity match;

    @ManyToOne(optional = false)
    @JoinColumn(name = "membre_id", nullable = false)
    private MemberEntity membre;

    @Column(name = "date_inscription")
    private LocalDateTime dateInscription;

    @OneToOne
    @JoinColumn(name = "paiement_id")
    private PaiementEntity paiement;
}