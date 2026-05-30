package be.angularpadelclub.Mapper;

import be.angularpadelclub.DTO.MembreDTO;
import be.angularpadelclub.Entity.MembreEntity;
import be.angularpadelclub.Entity.SiteEntity;
import be.angularpadelclub.Entity.TypeMembreEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MembreMapper {

    public MembreDTO toDTO(MembreEntity entity) {
        if (entity == null) {
            return null;
        }

        return new MembreDTO(
                entity.getId(),
                entity.isActif(),
                entity.getEmail(),
                entity.getMatricule(),
                entity.getPrenom(),
                entity.getNom(),
                entity.getType() != null ? entity.getType().getCode() : null,
                entity.getSite() != null ? entity.getSite().getId() : null,
                entity.getSite() != null ? entity.getSite().getNom() : null
        );
    }

    public MembreEntity toEntity(
            MembreDTO dto,
            TypeMembreEntity type,
            SiteEntity site
    ) {
        if (dto == null) {
            return null;
        }

        MembreEntity entity = new MembreEntity();

        entity.setId(dto.id());
        entity.setActif(dto.active());
        entity.setEmail(dto.email());
        entity.setMatricule(dto.matricule());
        entity.setPrenom(dto.firstName());
        entity.setNom(dto.lastName());
        entity.setType(type);
        entity.setSite(site);

        return entity;
    }

    public List<MembreDTO> toDTOList(
            List<MembreEntity> entities
    ) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(this::toDTO)
                .toList();
    }
}