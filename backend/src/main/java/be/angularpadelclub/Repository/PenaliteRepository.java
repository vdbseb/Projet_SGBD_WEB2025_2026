package be.angularpadelclub.Repository;

import be.angularpadelclub.Entity.PenaliteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PenaliteRepository
        extends JpaRepository<PenaliteEntity, Integer> {

    boolean existsByMembre_IdAndActiveTrue(Integer membreId);
}