package be.angularpadelclub.Mapper;

import be.angularpadelclub.DTO.MembreDTO;
import be.angularpadelclub.DTO.ReservationDetailDTO;
import be.angularpadelclub.Entity.MatchEntity;
import be.angularpadelclub.Entity.PaiementEntity;
import be.angularpadelclub.Entity.ParticipationEntity;
import be.angularpadelclub.Repository.PaiementRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReservationDetailMapper {

    private final MembreMapper membreMapper;
    private final PaiementRepository paiementRepository;

    public ReservationDetailMapper(
            MembreMapper membreMapper,
            PaiementRepository paiementRepository
    ) {
        this.membreMapper = membreMapper;
        this.paiementRepository = paiementRepository;
    }

    public ReservationDetailDTO toDetailsDTO(
            MatchEntity match,
            List<ParticipationEntity> participations,
            String currentMatricule
    ) {
        List<MembreDTO> members = participations.stream()
                .map(participation -> membreMapper.toDTO(participation.getMembre()))
                .toList();

        String myPaymentStatus = participations.stream()
                .filter(participation -> participation.getMembre()
                        .getMatricule()
                        .equals(currentMatricule))
                .map(this::paymentStatusForParticipation)
                .findFirst()
                .orElse("NOT_PARTICIPATING");

        return new ReservationDetailDTO(
                match.getId(),
                match.getDateMatch(),
                match.getHeureDebut(),
                match.getHeureFin(),

                match.getTerrain().getId(),
                match.getTerrain().getNom(),

                match.getTerrain().getSite().getId(),
                match.getTerrain().getSite().getNom(),

                match.getOrganisateur().getMatricule(),
                match.getOrganisateur().getPrenom() + " " + match.getOrganisateur().getNom(),

                members,
                myPaymentStatus
        );
    }

    private String paymentStatusForParticipation(
            ParticipationEntity participation
    ) {
        return paiementRepository
                .findFirstByParticipation_IdOrderByDateCreationDesc(
                        participation.getId()
                )
                .map(PaiementEntity::getStatut)
                .map(Enum::name)
                .orElse("NON_PAYE");
    }
}