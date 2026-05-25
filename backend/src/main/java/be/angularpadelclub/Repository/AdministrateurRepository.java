package be.angularpadelclub.Repository;

import be.angularpadelclub.Entity.AdministrateurEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdministrateurRepository extends JpaRepository<AdministrateurEntity, Integer> {

    List<AdministrateurEntity> findByTypeAdmin(String typeAdmin);

    List<AdministrateurEntity> findBySiteId(Integer siteId);

    Optional<AdministrateurEntity> findByMatricule(String matricule);

    boolean existsByMatricule(String matricule);
}