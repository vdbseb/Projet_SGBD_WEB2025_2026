package be.angularpadelclub.Mapper;

import be.angularpadelclub.DTO.PaiementDTO;
import be.angularpadelclub.Entity.PaiementEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaiementMapper {

    public PaiementDTO toDTO(PaiementEntity entity) {
        if (entity == null) {
            return null;
        }

        return new PaiementDTO(
                entity.getId(),
                entity.getReservation() != null
                        ? entity.getReservation().getId()
                        : null,
                entity.getParticipation() != null
                        ? entity.getParticipation().getId()
                        : null,
                entity.getMembre() != null
                        ? entity.getMembre().getId()
                        : null,
                entity.getMontantCentimes(),
                entity.getDevise(),
                entity.getProvider(),
                entity.getProviderPaymentId(),
                entity.getClientSecret(),
                entity.getMethode(),
                entity.getStatut(),
                entity.getDateCreation(),
                entity.getDatePaiement(),
                entity.getDateExpiration()
        );
    }

    public List<PaiementDTO> toDTOList(List<PaiementEntity> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(this::toDTO)
                .toList();
    }
}