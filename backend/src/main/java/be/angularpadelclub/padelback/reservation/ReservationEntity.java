package be.angularpadelclub.padelback.reservation;
import jakarta.persistence.*;

import java.util.UUID;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="reservations")
public class ReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDateTime createdAt;
}