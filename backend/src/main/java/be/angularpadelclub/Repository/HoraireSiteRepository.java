package be.angularpadelclub.Repository;

import be.angularpadelclub.Entity.HoraireSiteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HoraireSiteRepository
        extends JpaRepository<HoraireSiteEntity, Integer> {

    Optional<HoraireSiteEntity> findBySite_IdAndAnnee(
            Integer siteId,
            int annee
    );

    List<HoraireSiteEntity> findBySite_Id(
            Integer siteId
    );

    boolean existsBySite_IdAndAnnee(
            Integer siteId,
            int annee
    );
}