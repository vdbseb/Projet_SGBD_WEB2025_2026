package be.angularpadelclub.Repository;


import be.angularpadelclub.Entity.JourFermetureEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface JourFermetureRepository
        extends JpaRepository<JourFermetureEntity, Integer> {

    boolean existsBySiteIdAndDateFermeture(Integer siteId, LocalDate dateFermeture);

    boolean existsByGlobalTrueAndDateFermeture(LocalDate dateFermeture);
}