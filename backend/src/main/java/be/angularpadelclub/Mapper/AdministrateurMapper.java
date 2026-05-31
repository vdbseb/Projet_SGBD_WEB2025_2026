package be.angularpadelclub.Mapper;

import be.angularpadelclub.DTO.AdministrateurDTO;
import be.angularpadelclub.Entity.AdministrateurEntity;
import be.angularpadelclub.Entity.SiteEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdministrateurMapper {

    public AdministrateurDTO toDTO(AdministrateurEntity entity) {
        if (entity == null) {
            return null;
        }

        SiteEntity site = entity.getSite();

        Integer siteId = site != null ? site.getId() : null;
        String siteNom = site != null ? site.getNom() : null;

        return new AdministrateurDTO(
                entity.getId(),
                entity.getMatricule(),
                entity.getNom(),
                entity.getPrenom(),
                entity.getEmail(),
                entity.getTypeAdmin(),
                siteId,
                siteNom
        );
    }

    public List<AdministrateurDTO> toDTOList(List<AdministrateurEntity> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(this::toDTO)
                .toList();
    }
}