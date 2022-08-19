package nl.utwente.teamepic.tomyappointment.resources;

import nl.utwente.teamepic.tomyappointment.dao.ThemeDao;
import nl.utwente.teamepic.tomyappointment.exceptions.SQLCallException;
import nl.utwente.teamepic.tomyappointment.model.Theme;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import java.sql.SQLException;
import java.util.List;

@Path("allthemes")
public class AllThemesResource {
    @Context
    UriInfo uriInfo;
    @Context
    Request request;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Theme> getAllThemes() {
        try {
            return new ThemeDao().getAllOfAll();
        } catch (SQLException | ClassNotFoundException e) {
            throw new SQLCallException();
        }
    }
}
