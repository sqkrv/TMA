package nl.utwente.teamepic.tomyappointment.resources;

import nl.utwente.teamepic.tomyappointment.dao.MunicipalityDao;
import nl.utwente.teamepic.tomyappointment.exceptions.BadRequestException;
import nl.utwente.teamepic.tomyappointment.exceptions.GeneralException;
import nl.utwente.teamepic.tomyappointment.exceptions.SQLCallException;
import nl.utwente.teamepic.tomyappointment.model.Municipality;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import java.sql.SQLException;
import java.util.List;

import static nl.utwente.teamepic.tomyappointment.dao.MunicipalityDao.Columns.*;

@Path("municipalities")
public class MunicipalitiesResource {
    @Context
    UriInfo uriInfo;
    @Context
    Request request;

    @Path("{municipality}")
    public MunicipalityResource getMunicipality(@PathParam("municipality") String id) {
        return new MunicipalityResource(uriInfo, request, id);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Municipality> getMunicipalities() {
        try {
            return new MunicipalityDao().getAll();
        } catch (SQLException e) {
            throw new SQLCallException(e);
        } catch (ClassNotFoundException e) {
            throw new GeneralException();
        }
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String newTheme(@FormDataParam(ID) String id,
                           @FormDataParam(NAME) String name,
                           @FormDataParam(SHORT_NAME) String short_name) {
        Municipality municipality = new Municipality(id, name, short_name);

        if (!municipality.isFulFilled()) throw new BadRequestException("Some of the required data is not provided");

        try {
            String insertedId = new MunicipalityDao().insert(municipality);
            municipality.setID(insertedId);
        } catch (SQLException e) {
            throw new SQLCallException(e);
        } catch (ClassNotFoundException e) {
            throw new GeneralException();
        }

        return "New municipality has been successfully added with ID: " + municipality.getID();
    }
}
