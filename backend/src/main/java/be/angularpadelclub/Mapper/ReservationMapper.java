package be.angularpadelclub.Mapper;

import be.angularpadelclub.DTO.ReservationDTO;
import be.angularpadelclub.Entity.CourtEntity;
import be.angularpadelclub.Entity.MatchEntity;
import be.angularpadelclub.Entity.MembreEntity;
import be.angularpadelclub.Entity.ReservationEntity;
import be.angularpadelclub.Enum.ReservationStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReservationMapper {

    public ReservationDTO toDTO(ReservationEntity entity) {

        MatchEntity match = entity.getMatch();

        List<String> participantMatricules =
                match != null && match.getParticipations() != null
                        ? match.getParticipations()
                        .stream()
                        .map(p -> p.getMembre().getMatricule())
                        .toList()
                        : List.of();

        return new ReservationDTO(
                entity.getId(),
                entity.getDate(),
                entity.getEndTime(),
                entity.getStartTime(),
                entity.getCourt().getId(),
                entity.getCourt().getSite().getNom(),
                entity.getMember().getId(),
                entity.getStatut(),
                match != null ? match.getTypeMatch() : null,
                match != null ? match.getStatut() : null,
                participantMatricules
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
        entity.setStatut(
                dto.reservationStatus() != null
                        ? dto.reservationStatus()
                        : ReservationStatus.EN_ATTENTE_PAIEMENT
        );

        return entity;
    }

    public List<ReservationDTO> toDTOList(List<ReservationEntity> entities) {
        return entities.stream()
                .map(this::toDTO)
                .toList();
    }
}