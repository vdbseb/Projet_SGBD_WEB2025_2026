package be.angularpadelclub.DTO;

import java.util.List;

public class SiteDTO {

    private Integer id;
    private String city;
    private String clubName;
    private String description;
    private String image;
    private String initial;
    private List<CourtDTO> courts;

    public SiteDTO() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getClubName() {
        return clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getInitial() {
        return initial;
    }

    public void setInitial(String initial) {
        this.initial = initial;
    }

    public List<CourtDTO> getCourts() {
        return courts;
    }

    public void setCourts(List<CourtDTO> courts) {
        this.courts = courts;
    }
}
