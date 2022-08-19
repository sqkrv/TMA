package nl.utwente.teamepic.tomyappointment.exceptions;

public class MunicipalityNotFound extends NotFoundException {
    public MunicipalityNotFound() {
        super("Municipality not found");
    }

    public MunicipalityNotFound(String message) {
        super(message);
    }
}
