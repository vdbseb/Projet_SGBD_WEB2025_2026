package be.angularpadelclub.Repository;

import be.angularpadelclub.Entity.ParticipationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<ParticipationEntity, Integer> {

    List<ParticipationEntity> findByMatchId(Integer matchId);
    boolean existsByMatch_IdAndMembre_Id(Integer matchId, Integer membreId);
    int countByMatch_Id(Integer matchId);

    Optional<ParticipationEntity> findByMatch_IdAndMembre_Id(
            Integer matchId,
            Integer membreId
    );
}