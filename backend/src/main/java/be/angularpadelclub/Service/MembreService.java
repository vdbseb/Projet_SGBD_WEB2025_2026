package be.angularpadelclub.Service;

import be.angularpadelclub.DTO.MembreDTO;
import be.angularpadelclub.Entity.MembreEntity;
import be.angularpadelclub.Entity.SiteEntity;
import be.angularpadelclub.Mapper.MembreMapper;
import be.angularpadelclub.Repository.MembreRepository;
import be.angularpadelclub.Repository.SiteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MembreService {

    private final MembreRepository membreRepository;
    private final SiteRepository siteRepository;
    private final MembreMapper membreMapper;

    public MembreService(
            MembreRepository membreRepository,
            SiteRepository siteRepository,
            MembreMapper membreMapper
    ) {
        this.membreRepository = membreRepository;
        this.siteRepository = siteRepository;
        this.membreMapper = membreMapper;
    }

    public List<MembreEntity> findAll() {
        return membreRepository.findAll();
    }

    public Optional<MembreEntity> findById(int id) {
        return membreRepository.findById(id);
    }

    public Optional<MembreEntity> findByMatricule(String matricule) {
        return membreRepository.findByMatricule(matricule);
    }

    public List<MembreEntity> findByNomAndPrenom(
            String nom,
            String prenom
    ) {
        return membreRepository
                .findByNomIgnoreCaseAndPrenomIgnoreCase(nom, prenom);
    }

    public void addMember(MembreDTO dto) {

        if (membreRepository.existsByMatricule(dto.matricule())) {
            throw new RuntimeException("Matricule already exists.");
        }

        SiteEntity site = null;

        if (dto.siteId() != null) {
            site = siteRepository.findById(dto.siteId())
                    .orElseThrow(() -> new RuntimeException("Site not found"));
        }

        MembreEntity member = membreMapper.toEntity(dto, site);

        member.setId(null);

        membreRepository.save(member);
    }

    public void deleteMember(int id) {
        membreRepository.deleteById(id);
    }
}