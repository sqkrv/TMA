package nl.utwente.teamepic.tomyappointment.exceptions;

public class ThemeNotFound extends NotFoundException {
    public ThemeNotFound() {
        super("Theme not found");
    }

    public ThemeNotFound(String message) {
        super(message);
    }
}
