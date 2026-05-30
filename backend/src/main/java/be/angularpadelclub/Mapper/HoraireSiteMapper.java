package be.angularpadelclub.Mapper;

import be.angularpadelclub.DTO.HoraireSiteDTO;
import be.angularpadelclub.Entity.HoraireSiteEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HoraireSiteMapper {

    public HoraireSiteDTO toDTO(HoraireSiteEntity entity) {
        if (entity == null) {
            return null;
        }

        return new HoraireSiteDTO(
                entity.getId(),
                entity.getSite() != null ? entity.getSite().getId() : null,
                entity.getSite() != null ? entity.getSite().getNom() : null,
                entity.getAnnee(),
                entity.getHeure_debut(),
                entity.getHeure_fin(),
                entity.getDuree_match_minutes(),
                entity.getPause_minutes()
        );
    }

    public List<HoraireSiteDTO> toDTOList(
            List<HoraireSiteEntity> entities
    ) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(this::toDTO)
                .toList();
    }
}