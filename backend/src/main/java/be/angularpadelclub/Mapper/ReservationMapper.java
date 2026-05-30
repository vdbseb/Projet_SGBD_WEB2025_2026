package be.angularpadelclub.Mapper;

import be.angularpadelclub.DTO.ParticipantReservationDTO;
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

    private static final List<String> ACTIVE_PARTICIPATION_STATUSES = List.of(
            "EN_ATTENTE_PAIEMENT",
            "PAYEE"
    );

    public ReservationDTO toDTO(ReservationEntity entity) {
        if (entity == null) {
            return null;
        }

        MatchEntity match = entity.getMatch();

        List<ParticipantReservationDTO> participants =
                match != null && match.getParticipations() != null
                        ? match.getParticipations()
                        .stream()
                        .filter(participation -> participation.getMembre() != null)
                        .map(participation -> {
                            MembreEntity membre = participation.getMembre();

                            return new ParticipantReservationDTO(
                                    participation.getId(),
                                    membre.getId(),
                                    membre.getMatricule(),
                                    membre.getPrenom(),
                                    membre.getNom(),
                                    participation.getStatut() != null
                                            ? participation.getStatut().name()
                                            : null,
                                    participation.getMontantDuCentimes()
                            );
                        })
                        .toList()
                        : List.of();

        List<String> participantMatricules = participants.stream()
                .filter(participant -> isActiveParticipationStatus(participant.statut()))
                .map(ParticipantReservationDTO::matricule)
                .toList();

        return new ReservationDTO(
                entity.getId(),
                match != null ? match.getId() : null,
                entity.getDate(),
                entity.getEndTime(),
                entity.getStartTime(),
                entity.getCourt() != null ? entity.getCourt().getId() : null,
                entity.getCourt() != null && entity.getCourt().getSite() != null
                        ? entity.getCourt().getSite().getNom()
                        : null,
                entity.getMember() != null ? entity.getMember().getId() : null,
                entity.getStatut(),
                match != null ? match.getTypeMatch() : null,
                match != null ? match.getStatut() : null,
                participantMatricules,
                participants
        );
    }

    public ReservationEntity toEntity(
            ReservationDTO dto,
            CourtEntity court,
            MembreEntity member
    ) {
        if (dto == null) {
            return null;
        }

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

    public List<ReservationDTO> toDTOList(
            List<ReservationEntity> entities
    ) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(this::toDTO)
                .toList();
    }

    private boolean isActiveParticipationStatus(String status) {
        if (status == null) {
            return false;
        }

        return ACTIVE_PARTICIPATION_STATUSES.contains(
                status.trim().toUpperCase()
        );
    }
}