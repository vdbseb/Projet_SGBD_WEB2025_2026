package be.angularpadelclub.Mapper;

import be.angularpadelclub.DTO.MemberDTO;
import be.angularpadelclub.Entity.MemberEntity;
import be.angularpadelclub.Entity.SiteEntity;

public class MemberMapper {

    public static MemberDTO toDTO(MemberEntity entity) {
        if (entity == null) {
            return null;
        }

        MemberDTO dto = new MemberDTO();

        dto.setMatricule(entity.getMatricule());
        dto.setNom(entity.getNom());
        dto.setPrenom(entity.getPrenom());
        dto.setEmail(entity.getEmail());
        dto.setType(entity.getType());
        dto.setSoldeDu(entity.getSoldeDu());
        dto.setPenaliteJours(entity.getPenaliteJours());
        dto.setActif(entity.getActif());

        if (entity.getSite() != null) {
            dto.setSiteId(entity.getSite().getId());
            dto.setSiteNom(entity.getSite().getNom());
        }

        return dto;
    }

    public static MemberEntity toEntity(MemberDTO dto, SiteEntity site) {
        if (dto == null) {
            return null;
        }

        MemberEntity entity = new MemberEntity();

        entity.setMatricule(dto.getMatricule());
        entity.setNom(dto.getNom());
        entity.setPrenom(dto.getPrenom());
        entity.setEmail(dto.getEmail());
        entity.setType(dto.getType());
        entity.setSite(site);
        entity.setSoldeDu(dto.getSoldeDu() != null ? dto.getSoldeDu() : 0.0);
        entity.setPenaliteJours(dto.getPenaliteJours() != null ? dto.getPenaliteJours() : 0);
        entity.setActif(dto.getActif() != null ? dto.getActif() : true);

        return entity;
    }
}