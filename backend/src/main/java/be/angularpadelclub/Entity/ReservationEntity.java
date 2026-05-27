package be.angularpadelclub.Entity;

import be.angularpadelclub.Enum.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reservation")
public class ReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @ManyToOne(optional = false)
    @JoinColumn(name = "court_id", nullable = false)
    private CourtEntity court;

    @ManyToOne(optional = false)
    @JoinColumn(name = "membre_id", nullable = false)
    private MembreEntity member;

    @JsonIgnore
    @OneToOne(mappedBy = "reservation")
    private MatchEntity match;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus statut=ReservationStatus.EN_ATTENTE_PAIEMENT;
}