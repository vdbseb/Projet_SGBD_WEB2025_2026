package be.angularpadelclub.Repository;

import be.angularpadelclub.Entity.DetteMembreEntity;
import be.angularpadelclub.Enum.DetteStatut;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetteMembreRepository extends JpaRepository<DetteMembreEntity, Integer> {

    List<DetteMembreEntity> findByMembre_IdAndStatut(
            Integer membreId,
            DetteStatut statut
    );

    boolean existsByMembre_IdAndStatut(
            Integer membreId,
            DetteStatut statut
    );

    boolean existsByParticipation_IdAndStatut(
            Integer participationId,
            DetteStatut statut
    );
}
