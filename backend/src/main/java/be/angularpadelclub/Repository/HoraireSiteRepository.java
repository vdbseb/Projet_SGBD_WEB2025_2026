package be.angularpadelclub.Repository;

import be.angularpadelclub.Entity.HoraireSiteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface HoraireSiteRepository
        extends JpaRepository<HoraireSiteEntity, Integer> {

    Optional<HoraireSiteEntity> findBySite_IdAndAnnee(
            Integer siteId,
            int annee
    );
}