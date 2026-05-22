package be.angularpadelclub.Repository;

import be.angularpadelclub.Entity.MemberEntity;
import be.angularpadelclub.Entity.MemberType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository
        extends JpaRepository<MemberEntity, String> {

    List<MemberEntity> findByType(MemberType type);

    List<MemberEntity> findBySiteId(Integer siteId);

    boolean existsByMatricule(String matricule);

    boolean existsByEmail(String email);
}