package be.angularpadelclub.Repository;

import be.angularpadelclub.Entity.CourtEntity;
import be.angularpadelclub.Entity.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Integer> {

    boolean existsByCourtAndDateAndStartTimeLessThanAndEndTimeGreaterThan(
            CourtEntity court,
            LocalDate date,
            LocalTime endTime,
            LocalTime startTime
    );

    List<ReservationEntity> findByCourtIdAndDate(
            int courtId,
            LocalDate date
    );

    List<ReservationEntity> findByCourtAndDate(
            CourtEntity court,
            LocalDate date
    );

    boolean existsByCourtIdAndDateAfter(
            Integer courtId,
            LocalDate date
    );

    List<ReservationEntity> findByDate(
            LocalDate date
    );

    List<ReservationEntity> findByDateAndCourt_Site_Id(
            LocalDate date,
            Integer siteId
    );
    List<ReservationEntity> findByCourtIdAndDateGreaterThanEqual(
            Integer courtId,
            LocalDate date
    );
}