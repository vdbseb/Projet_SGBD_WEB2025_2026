package be.angularpadelclub.Repository;

import be.angularpadelclub.Entity.MembreEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MembreRepository extends JpaRepository<MembreEntity, Integer> {

    Optional<MembreEntity> findByMatricule(String matricule);
    List<MembreEntity> findByNomIgnoreCaseAndPrenomIgnoreCase(String nom, String prenom);

    boolean existsByMatricule(String matricule);
}