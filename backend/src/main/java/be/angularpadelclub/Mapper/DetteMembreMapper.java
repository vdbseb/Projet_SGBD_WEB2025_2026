package be.angularpadelclub.Mapper;

import be.angularpadelclub.DTO.DetteMembreDTO;
import be.angularpadelclub.Entity.DetteMembreEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DetteMembreMapper {

    public DetteMembreDTO toDTO(DetteMembreEntity entity) {
        return new DetteMembreDTO(
                entity.getId(),
                entity.getMembre().getId(),
                entity.getParticipation() != null
                        ? entity.getParticipation().getId()
                        : null,
                entity.getReservation() != null
                        ? entity.getReservation().getId()
                        : null,
                entity.getMontantCentimes(),
                entity.getStatut(),
                entity.getRaison(),
                entity.getDateCreation(),
                entity.getDateResolution()
        );
    }

    public List<DetteMembreDTO> toDTOList(List<DetteMembreEntity> entities) {
        return entities.stream()
                .map(this::toDTO)
                .toList();
    }
}
