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
        if (match == null) {
            return null;
        }

        List<ParticipationEntity> safeParticipations = participations == null
                ? List.of()
                : participations;

        List<MembreDTO> members = safeParticipations.stream()
                .filter(participation -> participation.getMembre() != null)
                .map(participation -> membreMapper.toDTO(participation.getMembre()))
                .toList();

        String myPaymentStatus = safeParticipations.stream()
                .filter(participation -> participation.getMembre() != null)
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

                match.getTerrain() != null ? match.getTerrain().getId() : null,
                match.getTerrain() != null ? match.getTerrain().getNom() : null,

                match.getTerrain() != null && match.getTerrain().getSite() != null
                        ? match.getTerrain().getSite().getId()
                        : null,
                match.getTerrain() != null && match.getTerrain().getSite() != null
                        ? match.getTerrain().getSite().getNom()
                        : null,

                match.getOrganisateur() != null
                        ? match.getOrganisateur().getMatricule()
                        : null,
                match.getOrganisateur() != null
                        ? match.getOrganisateur().getPrenom() + " " + match.getOrganisateur().getNom()
                        : null,

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