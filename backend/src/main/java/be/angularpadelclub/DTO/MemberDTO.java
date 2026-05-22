package be.angularpadelclub.DTO;

import be.angularpadelclub.Entity.MemberType;

public class MemberDTO {

    private String matricule;
    private String nom;
    private String prenom;
    private String email;
    private MemberType type;

    private Integer siteId;
    private String siteNom;

    private Double soldeDu;
    private Integer penaliteJours;
    private Boolean actif;

    public MemberDTO() {
    }

    public MemberDTO(String matricule, String nom, String prenom, String email, MemberType type,
                     Integer siteId, String siteNom, Double soldeDu, Integer penaliteJours, Boolean actif) {
        this.matricule = matricule;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.type = type;
        this.siteId = siteId;
        this.siteNom = siteNom;
        this.soldeDu = soldeDu;
        this.penaliteJours = penaliteJours;
        this.actif = actif;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public MemberType getType() {
        return type;
    }

    public void setType(MemberType type) {
        this.type = type;
    }

    public Integer getSiteId() {
        return siteId;
    }

    public void setSiteId(Integer siteId) {
        this.siteId = siteId;
    }

    public String getSiteNom() {
        return siteNom;
    }

    public void setSiteNom(String siteNom) {
        this.siteNom = siteNom;
    }

    public Double getSoldeDu() {
        return soldeDu;
    }

    public void setSoldeDu(Double soldeDu) {
        this.soldeDu = soldeDu;
    }

    public Integer getPenaliteJours() {
        return penaliteJours;
    }

    public void setPenaliteJours(Integer penaliteJours) {
        this.penaliteJours = penaliteJours;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }
}