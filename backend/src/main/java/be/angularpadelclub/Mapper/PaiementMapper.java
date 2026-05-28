package be.angularpadelclub.Mapper;

import be.angularpadelclub.DTO.PaiementDTO;
import be.angularpadelclub.Entity.PaiementEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaiementMapper {

    public PaiementDTO toDTO(PaiementEntity entity) {
        return new PaiementDTO(
                entity.getId(),
                entity.getReservation().getId(),
                entity.getParticipation() != null
                        ? entity.getParticipation().getId()
                        : null,
                entity.getMembre().getId(),
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
        return entities.stream()
                .map(this::toDTO)
                .toList();
    }
}
