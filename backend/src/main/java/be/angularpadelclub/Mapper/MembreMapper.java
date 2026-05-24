package be.angularpadelclub.Mapper;

import be.angularpadelclub.DTO.MembreDTO;
import be.angularpadelclub.Entity.MembreEntity;
import be.angularpadelclub.Entity.SiteEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MembreMapper {

    public MembreDTO toDTO(MembreEntity entity) {
        return new MembreDTO(
                entity.getId(),
                entity.isActif(),
                entity.getEmail(),
                entity.getMatricule(),
                entity.getPrenom(),
                entity.getNom(),
                entity.getType(),
                entity.getSite() != null ? entity.getSite().getId() : null,
                entity.getSite() != null ? entity.getSite().getNom() : null
        );
    }

    public MembreEntity toEntity(MembreDTO dto, SiteEntity site) {
        MembreEntity entity = new MembreEntity();

        entity.setId(dto.id());
        entity.setActif(dto.active());
        entity.setEmail(dto.email());
        entity.setMatricule(dto.matricule());
        entity.setPrenom(dto.firstName());
        entity.setNom(dto.lastName());
        entity.setType(dto.type());
        entity.setSite(site);

        return entity;
    }

    public List<MembreDTO> toDTOList(List<MembreEntity> entities) {
        return entities.stream()
                .map(this::toDTO)
                .toList();
    }
}