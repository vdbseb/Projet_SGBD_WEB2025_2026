package be.angularpadelclub.Service;

import be.angularpadelclub.DTO.MembreDTO;
import be.angularpadelclub.Entity.AdministrateurEntity;
import be.angularpadelclub.Entity.MembreEntity;
import be.angularpadelclub.Entity.SiteEntity;
import be.angularpadelclub.Mapper.MembreMapper;
import be.angularpadelclub.Repository.MembreRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class MembreService {

    private final MembreRepository membreRepository;
    private final MembreMapper membreMapper;
    private final ReferenceLookupService referenceLookupService;

    public MembreService(
            MembreRepository membreRepository,
            MembreMapper membreMapper,
            ReferenceLookupService referenceLookupService
    ) {
        this.membreRepository = membreRepository;
        this.membreMapper = membreMapper;
        this.referenceLookupService = referenceLookupService;
    }

    public List<MembreEntity> findAll() {
        return membreRepository.findAll();
    }

    public List<MembreEntity> findVisibleByAdmin(String adminMatricule) {
        AdministrateurEntity admin =
                referenceLookupService.findAdministrateurByMatriculeOrThrow(
                        adminMatricule
                );

        if (isGlobalAdmin(admin)) {
            return membreRepository.findAll();
        }

        if (isSiteAdmin(admin)) {
            SiteEntity adminSite = getAdminSiteOrThrow(admin);
            return membreRepository.findBySiteId(adminSite.getId());
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Accès refusé : type d'administrateur non autorisé."
        );
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

        return membreRepository.findByNomIgnoreCaseAndPrenomIgnoreCase(
                nom.trim(),
                prenom.trim()
        );
    }

    public void addMember(MembreDTO dto) {
        validateMemberCreateRequest(dto);

        if (membreRepository.existsByMatricule(dto.matricule())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Création impossible : le matricule existe déjà."
            );
        }

        SiteEntity site = resolveSiteForMember(dto);

        MembreEntity member = membreMapper.toEntity(dto, site);
        member.setId(null);

        membreRepository.save(member);
    }

    public void addMemberAsAdmin(
            String adminMatricule,
            MembreDTO dto
    ) {
        AdministrateurEntity admin =
                referenceLookupService.findAdministrateurByMatriculeOrThrow(
                        adminMatricule
                );

        if (isGlobalAdmin(admin)) {
            addMember(dto);
            return;
        }

        if (!isSiteAdmin(admin)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Création impossible : type d'administrateur non autorisé."
            );
        }

        SiteEntity adminSite = getAdminSiteOrThrow(admin);

        validateLocalAdminMemberCreation(dto);

        MembreDTO securedDto = new MembreDTO(
                dto.id(),
                dto.active(),
                dto.email(),
                dto.matricule(),
                dto.firstName(),
                dto.lastName(),
                dto.type(),
                adminSite.getId(),
                adminSite.getNom()
        );

        addMember(securedDto);
    }

    public void deleteMember(int id) {
        MembreEntity membre = referenceLookupService.findMembreOrThrow(id);

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
                                + "."
                );
            }
        }

        return prefix + String.format("%04d", nextNumber);
    }

    public MembreEntity updateMemberActiveStatus(
            int id,
            Boolean active
    ) {
        if (active == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le statut actif du membre est obligatoire."
            );
        }

        MembreEntity membre = referenceLookupService.findMembreOrThrow(id);

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

    public MembreEntity updateOwnProfile(
            int id,
            String firstName,
            String lastName,
            String email
    ) {
        validateOwnProfileUpdate(firstName, lastName, email);

        MembreEntity membre = referenceLookupService.findMembreOrThrow(id);

        if (!membre.isActif()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Modification impossible : ce membre est suspendu."
            );
        }

        membre.setPrenom(firstName.trim());
        membre.setNom(lastName.trim());
        membre.setEmail(email.trim());

        return membreRepository.save(membre);
    }

    private void validateOwnProfileUpdate(
            String firstName,
            String lastName,
            String email
    ) {
        if (firstName == null || firstName.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le prénom est obligatoire."
            );
        }

        if (lastName == null || lastName.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le nom est obligatoire."
            );
        }

        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "L'email est obligatoire."
            );
        }

        if (!email.contains("@") || !email.contains(".")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "L'email doit être valide."
            );
        }
    }

    private SiteEntity getAdminSiteOrThrow(AdministrateurEntity admin) {
        if (admin.getSite() == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Action impossible : cet administrateur local n'est rattaché à aucun site."
            );
        }

        return admin.getSite();
    }

    private boolean isGlobalAdmin(AdministrateurEntity admin) {
        return admin.getTypeAdmin() != null
                && "GLOBAL".equalsIgnoreCase(admin.getTypeAdmin());
    }

    private boolean isSiteAdmin(AdministrateurEntity admin) {
        return admin.getTypeAdmin() != null
                && "SITE".equalsIgnoreCase(admin.getTypeAdmin());
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
        validateSiteMatchesType(dto);
    }

    private void validateLocalAdminMemberCreation(MembreDTO dto) {
        if (dto == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Les données du membre sont obligatoires."
            );
        }

        if (dto.type() == null || dto.type().getCode() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le type de membre est obligatoire."
            );
        }

        if (!"SITE".equalsIgnoreCase(dto.type().getCode())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Un administrateur local ne peut créer que des membres de type SITE."
            );
        }

        if (dto.matricule() != null
                && !dto.matricule().isBlank()
                && !dto.matricule().toUpperCase().startsWith("S")) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Un administrateur local ne peut créer que des matricules commençant par S."
            );
        }
    }

    private void validateMatriculeMatchesType(MembreDTO dto) {
        String matricule = dto.matricule().trim().toUpperCase();
        String typeCode = dto.type().getCode().trim().toUpperCase();

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

    private void validateSiteMatchesType(MembreDTO dto) {
        String typeCode = dto.type().getCode().trim().toUpperCase();

        if ("SITE".equals(typeCode) && dto.siteId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Un membre SITE doit être rattaché à un site."
            );
        }

        if ("GLOBAL".equals(typeCode) && dto.siteId() != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Un membre GLOBAL ne doit pas être rattaché à un site."
            );
        }

        if ("LIBRE".equals(typeCode) && dto.siteId() != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Un membre LIBRE ne doit pas être rattaché à un site."
            );
        }
    }

    private SiteEntity resolveSiteForMember(MembreDTO dto) {
        if (dto.siteId() == null) {
            return null;
        }

        SiteEntity site = referenceLookupService.findSiteOrThrow(dto.siteId());

        if (!site.isActif()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Création impossible : le site " + site.getNom() + " est fermé."
            );
        }

        return site;
    }

    private String getPrefixForTypeCode(String typeCode) {
        if (typeCode == null || typeCode.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Type membre obligatoire."
            );
        }

        return switch (typeCode.trim().toUpperCase()) {
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