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
@Table(
        name = "reservation",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_reservation_court_date_start",
                        columnNames = {"court_id", "date", "start_time"}
                )
        }
)
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "court_id", nullable = false)
    private CourtEntity court;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membre_id", nullable = false)
    private MembreEntity member;

    @JsonIgnore
    @OneToOne(mappedBy = "reservation", fetch = FetchType.LAZY)
    private MatchEntity match;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReservationStatus statut = ReservationStatus.EN_ATTENTE_PAIEMENT;

    @PrePersist
    public void prePersist() {
        if (statut == null) {
            statut = ReservationStatus.EN_ATTENTE_PAIEMENT;
        }
    }
}