package be.angularpadelclub.padelback.reservation;

import be.angularpadelclub.padelback.court.CourtEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;


public interface ReservationRepository extends JpaRepository<ReservationEntity, UUID> {
    boolean existsByCourtAndDateAndStartTimeLessThanAndEndTimeGreaterThan(
            CourtEntity court,
            LocalDate date,
            LocalTime endTime,
            LocalTime startTime
    );
    List<ReservationEntity> findByCourtIdAndDate(UUID courtId, LocalDate date);
    List<ReservationEntity> findByCourtAndDate(CourtEntity court, LocalDate date);
}
