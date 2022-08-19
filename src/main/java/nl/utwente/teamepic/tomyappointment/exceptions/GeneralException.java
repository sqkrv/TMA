package nl.utwente.teamepic.tomyappointment.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class GeneralException extends WebApplicationException {
    public GeneralException() {
        this("Server has encountered an error");
    }

    public GeneralException(String message) {
        super(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message).type(MediaType.TEXT_PLAIN).build());
    }
}
