package be.angularpadelclub.Security;

import java.util.Objects;

public class AdminPrincipal {

    private final Integer adminId;
    private final String matricule;
    private final String role;
    private final Integer siteId;

    public AdminPrincipal(
            Integer adminId,
            String matricule,
            String role,
            Integer siteId
    ) {
        this.adminId = adminId;
        this.matricule = matricule;
        this.role = role;
        this.siteId = siteId;
    }

    public Integer getAdminId() {
        return adminId;
    }

    public String getMatricule() {
        return matricule;
    }

    public String getRole() {
        return role;
    }

    public Integer getSiteId() {
        return siteId;
    }

    public boolean isGlobalAdmin() {
        return Objects.equals(role, "GLOBAL");
    }

    public boolean isSiteAdmin() {
        return Objects.equals(role, "SITE");
    }
}