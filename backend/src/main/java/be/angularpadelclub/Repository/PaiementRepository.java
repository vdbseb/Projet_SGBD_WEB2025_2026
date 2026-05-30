package be.angularpadelclub.Repository;

import be.angularpadelclub.Entity.PaiementEntity;
import be.angularpadelclub.Enum.PaiementStatut;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaiementRepository extends JpaRepository<PaiementEntity, Integer> {

    List<PaiementEntity> findByReservation_Id(Integer reservationId);

    List<PaiementEntity> findByMembre_Id(Integer membreId);

    List<PaiementEntity> findByReservation_IdAndStatut(
            Integer reservationId,
            PaiementStatut statut
    );

    List<PaiementEntity> findByParticipation_Match_IdAndStatut(
            Integer matchId,
            PaiementStatut statut
    );

    List<PaiementEntity> findByParticipation_IdAndStatut(
            Integer participationId,
            PaiementStatut statut
    );

    List<PaiementEntity> findByMembre_IdAndParticipationIsNullAndReservationIsNullAndStatut(
            Integer membreId,
            PaiementStatut statut
    );

    Optional<PaiementEntity> findFirstByReservation_IdOrderByDateCreationDesc(
            Integer reservationId
    );

    Optional<PaiementEntity> findFirstByParticipation_IdOrderByDateCreationDesc(
            Integer participationId
    );

    Optional<PaiementEntity> findFirstByMembre_IdAndParticipationIsNullAndReservationIsNullAndStatutOrderByDateCreationDesc(
            Integer membreId,
            PaiementStatut statut
    );

    boolean existsByReservation_IdAndStatut(
            Integer reservationId,
            PaiementStatut statut
    );

    boolean existsByParticipation_IdAndStatut(
            Integer participationId,
            PaiementStatut statut
    );
}