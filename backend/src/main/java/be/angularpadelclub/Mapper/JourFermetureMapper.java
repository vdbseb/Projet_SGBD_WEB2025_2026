package be.angularpadelclub.Mapper;

import be.angularpadelclub.DTO.JourFermetureDTO;
import be.angularpadelclub.Entity.JourFermetureEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JourFermetureMapper {

    public JourFermetureDTO toDTO(JourFermetureEntity entity) {
        if (entity == null) {
            return null;
        }

        return new JourFermetureDTO(
                entity.getId(),
                entity.getSite() != null ? entity.getSite().getId() : null,
                entity.getSite() != null ? entity.getSite().getNom() : null,
                entity.getDateFermeture(),
                entity.getRaison(),
                entity.isGlobal()
        );
    }

    public List<JourFermetureDTO> toDTOList(
            List<JourFermetureEntity> entities
    ) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(this::toDTO)
                .toList();
    }
}