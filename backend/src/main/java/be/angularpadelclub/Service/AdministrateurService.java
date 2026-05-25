package be.angularpadelclub.Service;

import be.angularpadelclub.Entity.AdministrateurEntity;
import be.angularpadelclub.Repository.AdministrateurRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdministrateurService {

    private final AdministrateurRepository administrateurRepository;

    public AdministrateurService(
            AdministrateurRepository administrateurRepository
    ) {
        this.administrateurRepository =
                administrateurRepository;
    }

    public List<AdministrateurEntity> findAll() {
        return administrateurRepository.findAll();
    }

    public AdministrateurEntity findById(Integer id) {
        return administrateurRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Administrateur introuvable avec id : " + id
                        ));
    }

    public AdministrateurEntity findByMatricule(
            String matricule
    ) {
        return administrateurRepository
                .findByMatricule(matricule)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Administrateur introuvable avec matricule : "
                                        + matricule
                        ));
    }

    public List<AdministrateurEntity> findByTypeAdmin(
            String typeAdmin
    ) {
        return administrateurRepository
                .findByTypeAdmin(typeAdmin);
    }

    public List<AdministrateurEntity> findBySiteId(
            Integer siteId
    ) {
        return administrateurRepository
                .findBySiteId(siteId);
    }
}