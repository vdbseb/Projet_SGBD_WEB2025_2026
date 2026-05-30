package be.angularpadelclub.Repository;

import be.angularpadelclub.Entity.MatchEntity;
import be.angularpadelclub.Enum.MatchStatus;
import be.angularpadelclub.Enum.MatchType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface MatchRepository extends JpaRepository<MatchEntity, Integer> {

    List<MatchEntity> findByTypeMatchAndStatut(MatchType matchType, MatchStatus matchStatus);

}