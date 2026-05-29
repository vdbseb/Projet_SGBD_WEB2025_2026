package be.angularpadelclub.Repository;

import be.angularpadelclub.Entity.ParticipationEntity;
import be.angularpadelclub.Enum.ParticipationStatut;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<ParticipationEntity, Integer> {

    List<ParticipationEntity> findByMatchId(Integer matchId);
    boolean existsByMatch_IdAndMembre_Id(Integer matchId, Integer membreId);
    boolean existsByMatch_IdAndMembre_IdAndStatutIn(
            Integer matchId,
            Integer membreId,
            List<ParticipationStatut> statuts
    );

    int countByMatch_Id(Integer matchId);
    int countByMatch_IdAndStatutIn(
            Integer matchId,
            List<ParticipationStatut> statuts
    );

    Optional<ParticipationEntity> findByMatch_IdAndMembre_Id(
            Integer matchId,
            Integer membreId
    );

    Optional<ParticipationEntity> findFirstByMatch_IdAndMembre_IdAndStatutIn(
            Integer matchId,
            Integer membreId,
            List<ParticipationStatut> statuts
    );

    List<ParticipationEntity> findByStatutAndDateLimitePaiementBefore(
            ParticipationStatut statut,
            LocalDateTime dateLimite
    );
}
