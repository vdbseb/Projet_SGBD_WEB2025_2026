package be.angularpadelclub.Service;

import be.angularpadelclub.DTO.MembreDTO;
import be.angularpadelclub.Entity.MembreEntity;
import be.angularpadelclub.Entity.SiteEntity;
import be.angularpadelclub.Mapper.MembreMapper;
import be.angularpadelclub.Repository.MembreRepository;
import be.angularpadelclub.Repository.SiteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class MembreService {

    private final MembreRepository membreRepository;
    private final SiteRepository siteRepository;
    private final MembreMapper membreMapper;

    public MembreService(
            MembreRepository membreRepository,
            SiteRepository siteRepository,
            MembreMapper membreMapper
    ) {
        this.membreRepository = membreRepository;
        this.siteRepository = siteRepository;
        this.membreMapper = membreMapper;
    }

    public List<MembreEntity> findAll() {
        return membreRepository.findAll();
    }

    public Optional<MembreEntity> findById(int id) {
        return membreRepository.findById(id);
    }

    public Optional<MembreEntity> findByMatricule(String matricule) {
        return membreRepository.findByMatricule(matricule);
    }

    public List<MembreEntity> findByNomAndPrenom(
            String nom,
            String prenom
    ) {
        if (nom == null || nom.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le nom est obligatoire pour rechercher un membre."
            );
        }

        if (prenom == null || prenom.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le prénom est obligatoire pour rechercher un membre."
            );
        }

        return membreRepository
                .findByNomIgnoreCaseAndPrenomIgnoreCase(nom, prenom);
    }

    public void addMember(MembreDTO dto) {
        validateMemberCreateRequest(dto);

        if (membreRepository.existsByMatricule(dto.matricule())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Création impossible : le matricule existe déjà."
            );
        }

        SiteEntity site = null;

        if (dto.siteId() != null) {
            site = siteRepository.findById(dto.siteId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Création impossible : site introuvable avec l'id " + dto.siteId()
                    ));
        }

        MembreEntity member = membreMapper.toEntity(dto, site);
        member.setId(null);

        membreRepository.save(member);
    }

    /**
     * On évite la suppression physique des membres.
     * Un membre peut être lié à des réservations, participations, paiements ou dettes.
     * Pour le désactiver, utiliser updateMemberActiveStatus(...).
     */
    public void deleteMember(int id) {
        MembreEntity membre = findMemberOrThrow(id);

        if (!membre.isActif()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Le membre est déjà suspendu."
            );
        }

        membre.setActif(false);
        membreRepository.save(membre);
    }

    public String getNextMatricule(String typeCode) {
        String prefix = getPrefixForTypeCode(typeCode);

        String lastMatricule = membreRepository
                .findTopByMatriculeStartingWithOrderByMatriculeDesc(prefix)
                .map(MembreEntity::getMatricule)
                .orElse(null);

        int nextNumber = 1;

        if (lastMatricule != null) {
            String numericPart = lastMatricule.substring(1);

            try {
                nextNumber = Integer.parseInt(numericPart) + 1;
            } catch (NumberFormatException exception) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Génération impossible : dernier matricule invalide pour le préfixe "
                                + prefix
                                + " : "
                                + lastMatricule
                );
            }
        }

        return prefix + String.format("%04d", nextNumber);
    }

    public MembreEntity updateMemberActiveStatus(int id, Boolean active) {
        if (active == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le statut actif du membre est obligatoire."
            );
        }

        MembreEntity membre = findMemberOrThrow(id);

        if (membre.isActif() == active) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    active
                            ? "Le membre est déjà actif."
                            : "Le membre est déjà suspendu."
            );
        }

        membre.setActif(active);

        return membreRepository.save(membre);
    }

    private MembreEntity findMemberOrThrow(int id) {
        return membreRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Membre introuvable avec l'id " + id
                ));
    }

    private void validateMemberCreateRequest(MembreDTO dto) {
        if (dto == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Les données du membre sont obligatoires."
            );
        }

        if (dto.matricule() == null || dto.matricule().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le matricule est obligatoire."
            );
        }

        if (dto.firstName() == null || dto.firstName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le prénom est obligatoire."
            );
        }

        if (dto.lastName() == null || dto.lastName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le nom est obligatoire."
            );
        }

        if (dto.email() == null || dto.email().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "L'email est obligatoire."
            );
        }

        if (dto.type() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le type de membre est obligatoire."
            );
        }

        if (dto.type().getCode() == null || dto.type().getCode().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le code du type de membre est obligatoire."
            );
        }

        validateMatriculeMatchesType(dto);
    }

    private void validateMatriculeMatchesType(MembreDTO dto) {
        String matricule = dto.matricule().toUpperCase();
        String typeCode = dto.type().getCode().toUpperCase();

        if (!matricule.startsWith("G")
                && !matricule.startsWith("S")
                && !matricule.startsWith("L")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le matricule doit commencer par G, S ou L."
            );
        }

        if (matricule.startsWith("G") && !"GLOBAL".equals(typeCode)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Le matricule G doit correspondre à un membre GLOBAL."
            );
        }

        if (matricule.startsWith("S") && !"SITE".equals(typeCode)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Le matricule S doit correspondre à un membre SITE."
            );
        }

        if (matricule.startsWith("L") && !"LIBRE".equals(typeCode)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Le matricule L doit correspondre à un membre LIBRE."
            );
        }
    }

    private String getPrefixForTypeCode(String typeCode) {
        if (typeCode == null || typeCode.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Type membre obligatoire."
            );
        }

        return switch (typeCode.toUpperCase()) {
            case "GLOBAL" -> "G";
            case "SITE" -> "S";
            case "LIBRE" -> "L";
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Type membre inconnu : " + typeCode
            );
        };
    }
}