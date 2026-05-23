package be.angularpadelclub.Mapper;

import be.angularpadelclub.DTO.MemberDTO;
import be.angularpadelclub.DTO.ReservationDetailDTO;
import be.angularpadelclub.Entity.MatchEntity;
import be.angularpadelclub.Entity.ParticipationEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReservationDetailMapper {

    private final MemberMapper memberMapper;

    public ReservationDetailMapper(MemberMapper memberMapper) {
        this.memberMapper = memberMapper;
    }

    public ReservationDetailDTO toDetailsDTO(
            MatchEntity match,
            List<ParticipationEntity> participations,
            String currentMatricule
    ) {
        List<MemberDTO> members = participations.stream()
                .map(participation -> memberMapper.toDTO(participation.getMembre()))
                .toList();

        String myPaymentStatus = participations.stream()
                .filter(participation -> participation.getMembre()
                        .getMatricule()
                        .equals(currentMatricule))
                .map(participation -> participation.getPaiement() != null
                        ? participation.getPaiement().getStatut().name()
                        : "NON_PAYE")
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
}