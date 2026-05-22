package be.angularpadelclub.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import be.angularpadelclub.Repository.*;
import be.angularpadelclub.Entity.*;
import java.time.LocalDate;
import java.util.List;


public interface ReservationRepository extends JpaRepository<ReservationEntity, UUID> {
    List<ReservationEntity> findByCourtIdAndDate(Integer courtId, LocalDate date);
}
