package be.angularpadelclub.Mapper;

import be.angularpadelclub.DTO.ReservationDTO;
import be.angularpadelclub.Entity.CourtEntity;
import be.angularpadelclub.Entity.MemberEntity;
import be.angularpadelclub.Entity.ReservationEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReservationMapper {

    public ReservationDTO toDTO(ReservationEntity entity) {
        return new ReservationDTO(
                entity.getId(),
                entity.getCourt().getId(),
                entity.getCourt().getNom(),
                entity.getMember().getId(),
                entity.getMember().getMatricule(),
                entity.getDate(),
                entity.getStartTime(),
                entity.getEndTime()
        );
    }

    public ReservationEntity toEntity(
            ReservationDTO dto,
            CourtEntity court,
            MemberEntity member
    ) {
        ReservationEntity entity = new ReservationEntity();

        entity.setId(dto.id());
        entity.setCourt(court);
        entity.setMember(member);
        entity.setDate(dto.date());
        entity.setStartTime(dto.startTime());
        entity.setEndTime(dto.endTime());

        return entity;
    }

    public List<ReservationDTO> toDTOList(List<ReservationEntity> entities) {
        return entities.stream()
                .map(this::toDTO)
                .toList();
    }
}