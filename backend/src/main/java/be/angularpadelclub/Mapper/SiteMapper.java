package be.angularpadelclub.Mapper;

import be.angularpadelclub.DTO.CourtDTO;
import be.angularpadelclub.DTO.SiteDTO;
import be.angularpadelclub.Entity.CourtEntity;
import be.angularpadelclub.Entity.SiteEntity;

import java.util.List;

public class SiteMapper {

    public static SiteDTO toDTO(
            SiteEntity site,
            List<CourtEntity> courts
    ) {

        SiteDTO dto = new SiteDTO();

        dto.setId(site.getId());
        dto.setCity(site.getVille());
        dto.setClubName(site.getNom());
        dto.setDescription(site.getDescription());
        dto.setImage(site.getImageUrl());

        // B, L, A...
        dto.setInitial(
                site.getVille()
                        .substring(0, 1)
                        .toUpperCase()
        );

        dto.setCourts(
                courts.stream()
                        .map(CourtMapper::toDTO)
                        .toList()
        );

        return dto;
    }


}