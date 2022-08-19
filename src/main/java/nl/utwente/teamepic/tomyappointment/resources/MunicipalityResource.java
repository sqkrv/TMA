package nl.utwente.teamepic.tomyappointment.resources;

import nl.utwente.teamepic.tomyappointment.dao.MunicipalityDao;
import nl.utwente.teamepic.tomyappointment.exceptions.BadRequestException;
import nl.utwente.teamepic.tomyappointment.exceptions.*;
import nl.utwente.teamepic.tomyappointment.model.Municipality;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import java.sql.SQLException;
import java.util.List;

import static nl.utwente.teamepic.tomyappointment.dao.MunicipalityDao.Columns.NAME;
import static nl.utwente.teamepic.tomyappointment.dao.MunicipalityDao.Columns.SHORT_NAME;

public class MunicipalityResource {
    @Context
    UriInfo uriInfo;
    @Context
    Request request;
    String id;

    public MunicipalityResource() {
    }

    public MunicipalityResource(UriInfo uriInfo, Request request, String id) {
        this.uriInfo = uriInfo;
        this.request = request;
        this.id = id;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Municipality getMunicipality() throws SQLException, ClassNotFoundException {
        try {
            List<Municipality> municipalities = new MunicipalityDao().getById(id);

            if (municipalities.isEmpty())
                throw new MunicipalityNotFound();

            return municipalities.get(0);
        } catch (SQLException | ClassNotFoundException e) {
            throw new SQLCallException();
        }
    }

    @PATCH
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String updateTheme(@FormDataParam(NAME) String name,
                              @FormDataParam(SHORT_NAME) String short_name) {
        Municipality municipality = new Municipality(id, name, short_name);

        if (municipality.isBlank()) throw new BadRequestException("No data provided");

        if (!municipality.isBlank()) {
            try {
                int rowsUpdated = new MunicipalityDao().update(municipality);
                if (rowsUpdated <= 0)
                    throw new SQLCallException("Municipality has not been updated. Probably because it has not been found.");
            } catch (SQLException e) {
                if ((e.getMessage().contains("invalid input syntax for type uuid"))) throw new ThemeNotFound();
                throw new SQLCallException(e);
            } catch (ClassNotFoundException e) {
                throw new GeneralException();
            }
        }

        return "Municipality has been successfully updated";
    }

    @DELETE
    public String deleteTheme() {
        try {
            int rowsDeleted = new MunicipalityDao().delete(id);

            if (rowsDeleted == 0)
                throw new ThemeNotFound();

            return "Municipality has been deleted";
        } catch (SQLException e) {
            if ((e.getMessage().contains("invalid input syntax for type uuid"))) throw new ThemeNotFound();
            throw new SQLCallException(e);
        } catch (ClassNotFoundException e) {
            throw new GeneralException();
        }
    }

    @Path("themes")
    public ThemesResource getThemesResource() {
        return new ThemesResource(uriInfo, request, id);
    }
}
