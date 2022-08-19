package nl.utwente.teamepic.tomyappointment.resources;

import nl.utwente.teamepic.tomyappointment.dao.ThemeDao;
import nl.utwente.teamepic.tomyappointment.exceptions.BadRequestException;
import nl.utwente.teamepic.tomyappointment.exceptions.GeneralException;
import nl.utwente.teamepic.tomyappointment.exceptions.SQLCallException;
import nl.utwente.teamepic.tomyappointment.model.Theme;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static nl.utwente.teamepic.tomyappointment.dao.ThemeDao.Columns.*;

@Path("themes")
public class ThemesResource {
    @Context
    UriInfo uriInfo;
    @Context
    Request request;
    String municipalityId;

    public ThemesResource(UriInfo uriInfo, Request request, String municipalityId) {
        this.uriInfo = uriInfo;
        this.request = request;
        this.municipalityId = municipalityId;
    }

    @Path("{theme}")
    public ThemeResource getTheme(@PathParam("theme") String themeId) {
        return new ThemeResource(uriInfo, request, municipalityId, themeId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Theme> getThemes() {
        try {
            return new ThemeDao(municipalityId).getAll();
        } catch (SQLException | ClassNotFoundException e) {
            throw new SQLCallException();
        }
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String newTheme(@FormDataParam(ID) String id,
                           @FormDataParam(LOCATION) String location,
                           @FormDataParam(COLOUR) String colour,
                           @FormDataParam(HOME_URL) String home_url,
                           @FormDataParam(FONT_URL) String font_url,
                           @FormDataParam(FONT_FAMILY) String font_family,
                           @FormDataParam(IS_DEFAULT) boolean is_default,

                           @FormDataParam(BACKGROUND) InputStream background,
                           @FormDataParam(BACKGROUND) FormDataContentDisposition backgroundDisposition,
                           @FormDataParam(LOGO) InputStream logo,
                           @FormDataParam(LOGO) FormDataContentDisposition logoDisposition,
                           @FormDataParam(ICON_512) InputStream icon512,
                           @FormDataParam(ICON_512) FormDataContentDisposition icon512Disposition,
                           @FormDataParam(ICON_192) InputStream icon192,
                           @FormDataParam(ICON_192) FormDataContentDisposition icon192Disposition,
                           @FormDataParam(FONT_FILE) InputStream fontFile,
                           @FormDataParam(FONT_FILE) FormDataContentDisposition fontFileDisposition) {
        if (font_url == null && fontFile != null) font_url = fontFileDisposition.getFileName();

        Theme theme = new Theme(id, municipalityId, location, colour, home_url, font_url, font_family, is_default);

        if (
                !theme.isFulFilled() |
                        (backgroundDisposition != null && backgroundDisposition.getFileName().isEmpty()) |
                        (logoDisposition != null && logoDisposition.getFileName().isEmpty()) |
                        (icon512Disposition != null && icon512Disposition.getFileName().isEmpty()) |
                        (icon192Disposition != null && icon192Disposition.getFileName().isEmpty())
        ) throw new BadRequestException("Some of the required data is not provided");

        assert backgroundDisposition != null;
        assert logoDisposition != null;
        assert icon512Disposition != null;
        assert icon192Disposition != null;

        try {
            String insertedId = new ThemeDao(municipalityId).insert(theme);
            theme.setID(insertedId);
        } catch (SQLException e) {
            throw new SQLCallException(e);
        } catch (ClassNotFoundException e) {
            throw new GeneralException();
        }

        try {
            FileHandler fileHandler = new FileHandler(theme);

            List<InputStream> files = Arrays.asList(background, logo, icon512, icon192, fontFile);
            List<FormDataContentDisposition> filesDispositions = Arrays.asList(backgroundDisposition, logoDisposition, icon512Disposition, icon192Disposition, fontFileDisposition);

            for (int i=0; i < files.size(); i++) {
                FormDataContentDisposition fileDisposition = filesDispositions.get(i);
                if (fileDisposition != null)
                    if (fileHandler.checkFile(fileDisposition))
                        fileHandler.handleFileUpload(files.get(i), fileDisposition);
                    else throw new GeneralException("Malformed file provided");
            }
        } catch (IOException e) {
            throw new GeneralException("Couldn't handle files.\n\n" + e.getMessage());
        }

        return "New theme has been successfully added with ID: " + theme.getID();
    }
}
