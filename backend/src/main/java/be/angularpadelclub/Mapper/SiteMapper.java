package be.angularpadelclub.Mapper;

import be.angularpadelclub.DTO.SiteDTO;
import be.angularpadelclub.Entity.HoraireSiteEntity;
import be.angularpadelclub.Entity.SiteEntity;
import be.angularpadelclub.Service.SiteService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SiteMapper {

    public SiteDTO toDTO(SiteEntity entity, HoraireSiteEntity horaire) {
        return new SiteDTO(
                entity.getId(),
                entity.getNom(),
                entity.getVille(),
                entity.getAdresse(),
                entity.getDescription(),
                horaire.getHeure_debut(),
                horaire.getHeure_fin(),
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