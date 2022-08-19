package nl.utwente.teamepic.tomyappointment.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.beans.Transient;

@XmlRootElement
public class Theme {
    private String ID;
    private String municipality_id;
    private String location;
    private String colour;
    private String home_url;
    private String start_url = "/";
    private String font_url;
    private String font_family;
    private Boolean is_default;

    public Theme() {
    }

    public Theme(String ID,
                 String municipality_id,
                 String location,
                 String colour,
                 String home_url,
                 String font_url,
                 String font_family,
                 Boolean is_default) {
        this.ID = ID;
        this.municipality_id = municipality_id;
        this.location = location;
        this.colour = colour;
        this.home_url = home_url;
        this.font_url = font_url;
        this.font_family = font_family;
        this.is_default = is_default;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getMunicipality_id() {
        return municipality_id;
    }

    public void setMunicipality_id(String municipality_id) {
        this.municipality_id = municipality_id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public String getHome_url() {
        return home_url;
    }

    public void setHome_url(String home_url) {
        this.home_url = home_url;
    }

    public String getStart_url() {
        return start_url;
    }

    public void setStart_url(String start_url) {
        this.start_url = start_url;
    }

    public Boolean isIs_default() {
        return is_default;
    }

    public void setIs_default(boolean is_default) {
        this.is_default = is_default;
    }

    public String getFont_url() {
        return font_url;
    }

    public void setFont_url(String font_url) {
        this.font_url = font_url;
    }

    public String getFont_family() {
        return font_family;
    }

    public void setFont_family(String font_family) {
        this.font_family = font_family;
    }

    @Transient
    public boolean isBlank() {
        return location == null &
                colour == null &
                home_url == null &
                is_default == null &
                font_url == null &
                is_default == null;
    }

    @Transient
    public boolean isFulFilled() {
        return location != null &
                colour != null &
                home_url != null &
                is_default != null &
//                font_url != null &
                is_default != null;
    }
}
