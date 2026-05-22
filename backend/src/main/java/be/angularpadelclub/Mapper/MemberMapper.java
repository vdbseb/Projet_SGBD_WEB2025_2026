package be.angularpadelclub.Mapper;

import be.angularpadelclub.DTO.MemberDTO;
import be.angularpadelclub.Entity.MemberEntity;
import be.angularpadelclub.Entity.SiteEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MemberMapper {

    public MemberDTO toDTO(MemberEntity entity) {
        return new MemberDTO(
                entity.getId(),
                entity.isActif(),
                entity.getEmail(),
                entity.getMatricule(),
                entity.getPrenom(),
                entity.getNom(),
                entity.getType(),
                entity.getSite() != null ? entity.getSite().getId() : null,
                entity.getSite() != null ? entity.getSite().getNom() : null
        );
    }

    public MemberEntity toEntity(MemberDTO dto, SiteEntity site) {
        MemberEntity entity = new MemberEntity();

        entity.setId(dto.id());
        entity.setMatricule(dto.matricule());
        entity.setPrenom(dto.firstName());
        entity.setNom(dto.lastName());
        entity.setType(dto.type());
        entity.setSite(site);

        return entity;
    }

    public List<MemberDTO> toDTOList(List<MemberEntity> entities) {
        return entities.stream()
                .map(this::toDTO)
                .toList();
    }
}