package be.angularpadelclub.padelback.reservation;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReservationMapper {

    public ReservationDTO toDTO(ReservationEntity entity) {
        return new ReservationDTO(
                entity.getId(),
                entity.getCourtName(),
                entity.getDate(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getPlayerMatricule()
        );
    }

    public List<ReservationDTO> toDTOList(List<ReservationEntity> entities) {
        return entities.stream()
                .map(this::toDTO)
                .toList();
    }

    public ReservationEntity toEntity(ReservationDTO dto) {
        ReservationEntity entity = new ReservationEntity();

        entity.setId(dto.id());
        entity.setCourtName(dto.courtName());
        entity.setDate(dto.date());
        entity.setStartTime(dto.startTime());
        entity.setEndTime(dto.endTime());
        entity.setPlayerMatricule(dto.playerMatricule());

        return entity;
    }
}