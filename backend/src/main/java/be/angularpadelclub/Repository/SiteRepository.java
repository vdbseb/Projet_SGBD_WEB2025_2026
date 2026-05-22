package be.angularpadelclub.Repository;

import be.angularpadelclub.Entity.SiteEntity;
import org.springframework.data.jpa.repository.JpaRepository;



public interface SiteRepository extends JpaRepository<SiteEntity, Integer> {
}