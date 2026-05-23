package be.angularpadelclub.Mapper;

import be.angularpadelclub.DTO.SiteDTO;
import be.angularpadelclub.Entity.HoraireSiteEntity;
import be.angularpadelclub.Entity.SiteEntity;
import org.springframework.stereotype.Component;

@Component
public class SiteMapper {

    public SiteDTO toDTO(SiteEntity entity, HoraireSiteEntity horaire) {
        var openingTime = horaire != null ? horaire.getHeure_debut() : null;
        var closingTime = horaire != null ? horaire.getHeure_fin() : null;

        return new SiteDTO(
                entity.getId(),
                entity.getNom(),
                entity.getVille(),
                entity.getAdresse(),
                entity.getDescription(),
                openingTime,
                closingTime,
                entity.isActif(),
                entity.getImage_url()
        );
    }
    public SiteEntity toEntity(SiteDTO dto) {
        SiteEntity entity = new SiteEntity();

        entity.setId(dto.id());
        entity.setNom(dto.name());
        entity.setVille(dto.city());
        entity.setAdresse(dto.adresse());
        entity.setDescription(dto.description());
        entity.setActif(dto.active());
        entity.setImage_url(dto.imageURL());

        return entity;
    }

}
