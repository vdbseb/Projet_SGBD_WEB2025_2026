package be.angularpadelclub.Mapper;

import be.angularpadelclub.DTO.ReservationDTO;
import be.angularpadelclub.Entity.CourtEntity;
import be.angularpadelclub.Entity.MembreEntity;
import be.angularpadelclub.Entity.ReservationEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReservationMapper {

    public ReservationDTO toDTO(ReservationEntity entity) {
        return new ReservationDTO(
                entity.getId(),
                entity.getDate(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getCourt().getId(),
                entity.getMember().getId(),
                null,
                List.of()
        );
    }



    public ReservationEntity toEntity(
            ReservationDTO dto,
            CourtEntity court,
            MembreEntity member
    ) {
        ReservationEntity entity = new ReservationEntity();

        entity.setId(dto.id());
        entity.setDate(dto.date());
        entity.setStartTime(dto.startTime());
        entity.setEndTime(dto.endTime());
        entity.setCourt(court);
        entity.setMember(member);

        return entity;
    }

    public List<ReservationDTO> toDTOList(List<ReservationEntity> entities) {
        return entities.stream()
                .map(this::toDTO)
                .toList();
    }
}