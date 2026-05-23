package be.angularpadelclub.Repository;

import be.angularpadelclub.Entity.ParticipationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipationRepository extends JpaRepository<ParticipationEntity, Integer> {

    List<ParticipationEntity> findByMatchId(Integer matchId);
}