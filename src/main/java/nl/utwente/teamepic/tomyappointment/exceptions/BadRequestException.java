package nl.utwente.teamepic.tomyappointment.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class BadRequestException extends WebApplicationException {
    public BadRequestException() {
        this("Malformed request received");
    }

    public BadRequestException(String message) {
        super(Response.status(Response.Status.BAD_REQUEST).entity(message).type(MediaType.TEXT_PLAIN).build());
    }
}
