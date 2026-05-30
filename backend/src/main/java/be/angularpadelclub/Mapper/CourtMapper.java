package be.angularpadelclub.Mapper;

import be.angularpadelclub.DTO.CourtDTO;
import be.angularpadelclub.Entity.CourtEntity;
import be.angularpadelclub.Entity.SiteEntity;
import org.springframework.stereotype.Component;

@Component
public class CourtMapper {

    public CourtDTO toDTO(CourtEntity entity) {
        if (entity == null) {
            return null;
        }

        return new CourtDTO(
                entity.getId(),
                entity.getNom(),
                entity.getSite() != null ? entity.getSite().getId() : null,
                entity.isCouvert(),
                entity.isActif(),
                entity.isMaintenance()
        );
    }

    public CourtEntity toEntity(
            CourtDTO dto,
            SiteEntity site
    ) {
        if (dto == null) {
            return null;
        }

        CourtEntity entity = new CourtEntity();

        entity.setId(dto.id());
        entity.setNom(dto.name());
        entity.setSite(site);
        entity.setCouvert(dto.indoor());
        entity.setActif(dto.active());
        entity.setMaintenance(dto.maintenance());

        return entity;
    }
}