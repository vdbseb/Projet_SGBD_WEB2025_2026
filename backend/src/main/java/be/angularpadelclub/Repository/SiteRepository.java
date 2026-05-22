package be.angularpadelclub.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import be.angularpadelclub.Entity.SiteEntity;

public interface SiteRepository
        extends JpaRepository<SiteEntity, Integer> {
}