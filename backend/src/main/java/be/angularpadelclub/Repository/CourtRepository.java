package be.angularpadelclub.Repository;

import be.angularpadelclub.Entity.CourtEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface CourtRepository extends JpaRepository<CourtEntity, Integer> {
    List<CourtEntity> findByActifTrue();

    List<CourtEntity> findBySiteIdAndActifTrue(Integer siteId);
}