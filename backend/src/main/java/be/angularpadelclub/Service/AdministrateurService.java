package be.angularpadelclub.Service;

import be.angularpadelclub.Entity.AdministrateurEntity;
import be.angularpadelclub.Repository.AdministrateurRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AdministrateurService {

    private final AdministrateurRepository administrateurRepository;
    private final ReferenceLookupService referenceLookupService;

    public AdministrateurService(
            AdministrateurRepository administrateurRepository,
            ReferenceLookupService referenceLookupService
    ) {
        this.administrateurRepository = administrateurRepository;
        this.referenceLookupService = referenceLookupService;
    }

    public List<AdministrateurEntity> findAll() {
        return administrateurRepository.findAll();
    }

    public AdministrateurEntity findById(Integer id) {
        return referenceLookupService.findAdministrateurOrThrow(id);
    }

    public AdministrateurEntity findByMatricule(
            String matricule
    ) {
        return referenceLookupService.findAdministrateurByMatriculeOrThrow(
                matricule
        );
    }

    public List<AdministrateurEntity> findByTypeAdmin(
            String typeAdmin
    ) {
        return administrateurRepository.findByTypeAdmin(typeAdmin);
    }

    public List<AdministrateurEntity> findBySiteId(
            Integer siteId
    ) {
        return administrateurRepository.findBySiteId(siteId);
    }
}