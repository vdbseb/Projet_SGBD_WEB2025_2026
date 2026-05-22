package be.angularpadelclub.Mapper;

import be.angularpadelclub.DTO.CourtDTO;
import be.angularpadelclub.Entity.CourtEntity;

public class CourtMapper {

    public static CourtDTO toDTO(CourtEntity court) {
        CourtDTO dto = new CourtDTO();

        dto.setId(court.getId());
        dto.setName("Terrain " + court.getName());
        dto.setType(court.getCouvert() ? "Indoor" : "Outdoor");

        return dto;
    }
}
