package be.angularpadelclub.Mapper;

import be.angularpadelclub.padelback.DTO.CourtDTO;
import be.angularpadelclub.padelback.Entity.CourtEntity;

public class CourtMapper {

    public static CourtDTO toDTO(CourtEntity court) {
        CourtDTO dto = new CourtDTO();

        dto.setId(court.getId());
        dto.setName("Terrain " + court.getNumero());
        dto.setType(court.getCouvert() ? "Indoor" : "Outdoor");

        return dto;
    }
}
