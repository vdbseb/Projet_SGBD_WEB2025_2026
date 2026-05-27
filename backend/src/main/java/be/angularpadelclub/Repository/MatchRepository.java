package be.angularpadelclub.Repository;

import be.angularpadelclub.Entity.MatchEntity;
import be.angularpadelclub.Enum.MatchStatus;
import be.angularpadelclub.Enum.MatchType;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface MatchRepository extends JpaRepository<MatchEntity, Integer> {


    List<MatchEntity> findByTypeMatchAndStatut(MatchType matchType, MatchStatus matchStatus);
}