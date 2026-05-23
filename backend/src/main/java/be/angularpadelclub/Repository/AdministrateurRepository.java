package be.angularpadelclub.Repository;

import be.angularpadelclub.Entity.AdministrateurEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdministrateurRepository extends JpaRepository<AdministrateurEntity, Integer> {

    List<AdministrateurEntity> findByTypeAdmin(String typeAdmin);

    List<AdministrateurEntity> findBySiteId(Integer siteId);
}