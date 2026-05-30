package be.angularpadelclub.Mapper;

import be.angularpadelclub.DTO.CourtDTO;
import be.angularpadelclub.DTO.SiteDTO;
import be.angularpadelclub.Entity.HoraireSiteEntity;
import be.angularpadelclub.Entity.SiteEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SiteMapper {

    public SiteDTO toDTO(
            SiteEntity entity,
            HoraireSiteEntity horaire
    ) {
        if (entity == null) {
            return null;
        }

        List<CourtDTO> courts = entity.getCourts() == null
                ? List.of()
                : entity.getCourts().stream()
                .map(court -> new CourtDTO(
                        court.getId(),
                        court.getNom(),
                        court.getSite() != null ? court.getSite().getId() : null,
                        court.isCouvert(),
                        court.isActif(),
                        court.isMaintenance()
                ))
                .toList();

        return new SiteDTO(
                entity.getId(),
                entity.getNom(),
                entity.getVille(),
                entity.getAdresse(),
                entity.getCode_postal(),
                entity.getDescription(),
                horaire != null ? horaire.getHeure_debut() : null,
                horaire != null ? horaire.getHeure_fin() : null,
                entity.isActif(),
                entity.getImage_url(),
                courts
        );
    }

    public SiteEntity toEntity(SiteDTO dto) {
        if (dto == null) {
            return null;
        }

        SiteEntity entity = new SiteEntity();

        entity.setId(dto.id());
        entity.setNom(dto.name());
        entity.setVille(dto.city());
        entity.setAdresse(dto.adresse());
        entity.setCode_postal(dto.codePostal());
        entity.setDescription(dto.description());
        entity.setActif(dto.active());
        entity.setImage_url(dto.imageUrl());

        return entity;
    }
}