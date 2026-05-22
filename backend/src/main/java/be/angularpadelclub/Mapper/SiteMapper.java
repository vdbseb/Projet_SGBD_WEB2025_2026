package be.angularpadelclub.Mapper;

import be.angularpadelclub.padelback.DTO.CourtDTO;
import be.angularpadelclub.padelback.DTO.SiteDTO;
import be.angularpadelclub.padelback.Entity.CourtEntity;
import be.angularpadelclub.padelback.Entity.SiteEntity;

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
                        .map(CourtMapper::courtToDTO)
                        .toList()
        );

        return dto;
    }


}