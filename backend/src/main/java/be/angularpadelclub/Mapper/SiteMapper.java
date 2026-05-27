package be.angularpadelclub.Mapper;

import be.angularpadelclub.DTO.CourtDTO;
import be.angularpadelclub.DTO.SiteDTO;
import be.angularpadelclub.Entity.HoraireSiteEntity;
import be.angularpadelclub.Entity.SiteEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SiteMapper {

    public SiteDTO toDTO(SiteEntity entity, HoraireSiteEntity horaire) {

        List<CourtDTO> courts = entity.getCourts() == null
                ? List.of()
                : entity.getCourts().stream()
                .map(court -> new CourtDTO(
                        court.getId(),
                        court.getNom(),
                        entity.getId(),
                        court.isCouvert(),
                        court.isActif()
                ))
                .toList();

        return new SiteDTO(
                entity.getId(),
                entity.getNom(),
                entity.getVille(),
                entity.getAdresse(),
                entity.getCode_postal(),
                entity.getDescription(),
                horaire.getHeure_debut(),
                horaire.getHeure_fin(),
                entity.isActif(),
                entity.getImage_url(),
                courts
        );
    }

    public SiteEntity toEntity(SiteDTO dto) {
        SiteEntity entity = new SiteEntity();

        entity.setId(dto.id());
        entity.setNom(dto.name());
        entity.setVille(dto.city());
        entity.setAdresse(dto.adresse());
        entity.setCode_postal(dto.codePostal());
        entity.setDescription(dto.description());
        entity.setActif(dto.active());
        entity.setImage_url(dto.imageURL());

        return entity;
    }
}