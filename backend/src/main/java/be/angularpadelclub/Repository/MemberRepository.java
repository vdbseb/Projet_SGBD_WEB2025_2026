package be.angularpadelclub.Repository;

import be.angularpadelclub.Entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface MemberRepository extends JpaRepository<MemberEntity, Integer> {

    Optional<MemberEntity> findByMatricule(String matricule);

    boolean existsByMatricule(String matricule);
}