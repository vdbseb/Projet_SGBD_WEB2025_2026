package be.angularpadelclub.Repository;

import be.angularpadelclub.Entity.PenaliteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PenaliteRepository
        extends JpaRepository<PenaliteEntity, Integer> {

    boolean existsByMembre_IdAndActiveTrue(Integer membreId);
    boolean existsByMembre_IdAndActiveTrueAndDateFinGreaterThanEqual(
            Integer membreId,
            LocalDate date
    );

    List<PenaliteEntity> findByActiveTrueAndDateFinLessThanEqual(LocalDate date);
}