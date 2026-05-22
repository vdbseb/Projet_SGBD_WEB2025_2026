package be.angularpadelclub.Repository;

import be.angularpadelclub.Entity.CourtEntity;
import org.springframework.data.jpa.repository.JpaRepository;



public interface CourtRepository extends JpaRepository<CourtEntity, Integer> {
}