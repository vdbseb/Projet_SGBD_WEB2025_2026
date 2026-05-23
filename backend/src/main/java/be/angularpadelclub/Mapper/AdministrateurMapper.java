package be.angularpadelclub.Mapper;

import be.angularpadelclub.DTO.AdministrateurDTO;
import be.angularpadelclub.Entity.AdministrateurEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdministrateurMapper {

    public AdministrateurDTO toDTO(AdministrateurEntity entity) {
        return new AdministrateurDTO(
                entity.getId(),
                entity.getNom(),
                entity.getPrenom(),
                entity.getEmail(),
                entity.getTypeAdmin(),
                entity.getSite() != null ? entity.getSite().getId() : null,
                entity.getSite() != null ? entity.getSite().getNom() : null
        );
    }

    public List<AdministrateurDTO> toDTOList(List<AdministrateurEntity> entities) {
        return entities.stream()
                .map(this::toDTO)
                .toList();
    }
}