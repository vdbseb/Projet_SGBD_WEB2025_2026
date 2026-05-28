package be.angularpadelclub.Mapper;

import be.angularpadelclub.DTO.JourFermetureDTO;
import be.angularpadelclub.Entity.JourFermetureEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JourFermetureMapper {

    public JourFermetureDTO toDTO(JourFermetureEntity entity) {
        return new JourFermetureDTO(
                entity.getId(),
                entity.getSite() == null ? null : entity.getSite().getId(),
                entity.getSite() == null ? null : entity.getSite().getNom(),
                entity.getDateFermeture(),
                entity.getRaison(),
                entity.isGlobal()
        );
    }

    public List<JourFermetureDTO> toDTOList(List<JourFermetureEntity> entities) {
        return entities.stream()
                .map(this::toDTO)
                .toList();
    }
}