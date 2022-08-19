package nl.utwente.teamepic.tomyappointment.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.beans.Transient;

@XmlRootElement
public class Municipality {
    private String ID;
    private String name;
    private String shortName;

    public Municipality(String ID, String name, String shortName) {
        this.ID = ID;
        this.name = name;
        this.shortName = shortName;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    @Transient
    public boolean isBlank() {
        return name == null &
                shortName == null;
    }

    @Transient
    public boolean isFulFilled() {
        return name != null && !name.isBlank() &&
                shortName != null && !shortName.isBlank();
    }
}
