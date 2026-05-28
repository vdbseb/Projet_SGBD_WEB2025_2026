package be.angularpadelclub.Repository;

import be.angularpadelclub.Entity.JourFermetureEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JourFermetureRepository
        extends JpaRepository<JourFermetureEntity, Integer> {

    boolean existsBySiteIdAndDateFermeture(
            Integer siteId,
            LocalDate dateFermeture
    );

    boolean existsByGlobalTrueAndDateFermeture(
            LocalDate dateFermeture
    );

    List<JourFermetureEntity> findBySiteId(
            Integer siteId
    );

    List<JourFermetureEntity> findByGlobalTrue();

    Optional<JourFermetureEntity> findBySiteIdAndDateFermeture(
            Integer siteId,
            LocalDate dateFermeture
    );

    Optional<JourFermetureEntity> findByGlobalTrueAndDateFermeture(
            LocalDate dateFermeture
    );
}