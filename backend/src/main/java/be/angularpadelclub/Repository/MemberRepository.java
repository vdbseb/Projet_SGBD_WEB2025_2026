package be.angularpadelclub.Repository;

import be.angularpadelclub.Entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, Integer> {

    Optional<MemberEntity> findByMatricule(String matricule);
    List<MemberEntity> findByNomIgnoreCaseAndPrenomIgnoreCase(String nom, String prenom);

    boolean existsByMatricule(String matricule);
}