package be.angularpadelclub.Repository;

import be.angularpadelclub.Entity.MatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<MatchEntity, Integer> {

}