package nl.utwente.teamepic.tomyappointment.resources;

import nl.utwente.teamepic.tomyappointment.dao.MunicipalityDao;
import nl.utwente.teamepic.tomyappointment.dao.ThemeDao;
import nl.utwente.teamepic.tomyappointment.exceptions.BadRequestException;
import nl.utwente.teamepic.tomyappointment.exceptions.*;
import nl.utwente.teamepic.tomyappointment.model.Municipality;
import nl.utwente.teamepic.tomyappointment.model.Theme;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static nl.utwente.teamepic.tomyappointment.dao.ThemeDao.Columns.*;

public class ThemeResource {
    @Context
    UriInfo uriInfo;
    @Context
    Request request;
    String municipalityId;
    String themeId;

    public ThemeResource(UriInfo uriInfo, Request request, String municipalityId, String themeId) {
        this.uriInfo = uriInfo;
        this.request = request;
        this.municipalityId = municipalityId;
        this.themeId = themeId;
    }

    public ThemeResource() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Theme getTheme() {
        try {
            List<Theme> themes = new ThemeDao(municipalityId).getById(themeId);

            if (themes.isEmpty())
                throw new ThemeNotFound();

            return themes.get(0);
        } catch (SQLException e) {
            if ((e.getMessage().contains("invalid input syntax for type uuid"))) throw new ThemeNotFound();
            throw new SQLCallException(e);
        } catch (ClassNotFoundException e) {
            throw new GeneralException();
        }
    }

    @PATCH
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String updateTheme(@FormDataParam(LOCATION) String location,
                              @FormDataParam(COLOUR) String colour,
                              @FormDataParam(HOME_URL) String home_url,
                              @FormDataParam(FONT_URL) String font_url,
                              @FormDataParam(FONT_FAMILY) String font_family,
                              @FormDataParam(IS_DEFAULT) Boolean is_default,

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
        Theme theme = new Theme(themeId, municipalityId, location, colour, home_url, font_url, font_family, is_default);

        if (theme.isBlank() &
                (backgroundDisposition == null || backgroundDisposition.getFileName().isEmpty()) &
                (logoDisposition == null || logoDisposition.getFileName().isEmpty()) &
                (icon512Disposition == null || icon512Disposition.getFileName().isEmpty()) &
                (icon192Disposition == null || icon192Disposition.getFileName().isEmpty())
        ) throw new BadRequestException("No data provided");

        if (!theme.isBlank()) {
            try {
                int rowsUpdated = new ThemeDao(municipalityId).update(theme);
//                if (rowsUpdated > 0) return "Theme has been successfully updated";
//                else throw new SQLCallException("Theme has not been updated. Probably because it has not been found.");
                if (rowsUpdated <= 0)
                    throw new SQLCallException("Theme has not been updated. Probably because it has not been found.");
            } catch (SQLException e) {
                if ((e.getMessage().contains("invalid input syntax for type uuid"))) throw new ThemeNotFound();
                throw new SQLCallException(e);
            } catch (ClassNotFoundException e) {
                throw new GeneralException();
            }
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
            }
        } catch (IOException e) {
            throw new GeneralException("Couldn't handle files.\n\n" + e.getMessage());
        }

        return "Theme has been successfully updated";
    }

    @DELETE
    public String deleteTheme() {
        try {
            int rowsDeleted = new ThemeDao(municipalityId).delete(themeId);

            if (rowsDeleted == 0)
                throw new ThemeNotFound();

            return "Theme has been deleted";
        } catch (SQLException e) {
            if ((e.getMessage().contains("invalid input syntax for type uuid"))) throw new ThemeNotFound();
            throw new SQLCallException(e);
        } catch (ClassNotFoundException e) {
            throw new GeneralException();
        }
    }

    private void writeJsonObject(ZipOutputStream zip, JSONObject jsonObject, String jsonName) throws IOException {
        ZipEntry entry = new ZipEntry(themeId + "/" + jsonName + ".json");
        zip.putNextEntry(entry);
        byte[] bytes = new byte[1024];
        int read;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(jsonObject.toString(4).getBytes());
        while ((read = byteArrayInputStream.read(bytes)) != -1) {
            zip.write(bytes, 0, read);
        }
        byteArrayInputStream.close();
    }

    @GET
    @Path("export")
    @Produces("application/zip")
    public Response exportTheme() throws IOException, SQLException, ClassNotFoundException {
        List<Theme> themes = new ThemeDao(municipalityId).getById(themeId);
        if (themes.isEmpty())
            throw new ThemeNotFound();
        Theme theme = themes.get(0);

        List<Municipality> municipalities = new MunicipalityDao().getById(municipalityId);
        if (municipalities.isEmpty())
            throw new MunicipalityNotFound();
        Municipality municipality = municipalities.get(0);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(out);
        ArrayList<String> files = new ArrayList<>(Arrays.asList("background.jpg", "logo.png", "icon-512.png", "icon-192.png"));
        if (theme.getFont_url() != null && !theme.getFont_url().startsWith("http")) files.add(theme.getFont_url());
        FileHandler fileHandler = new FileHandler(theme);
        java.nio.file.Path resolvedPath = fileHandler.resolvePath();

        // adding files
        for (String filePath : files) {
            File file = resolvedPath.resolve(filePath).toFile();
            FileInputStream fileStream = new FileInputStream(file);
            ZipEntry entry = new ZipEntry(themeId + "/" + file.getName());
            zip.putNextEntry(entry);

            byte[] bytes = new byte[1024];
            int read;
            while ((read = fileStream.read(bytes)) != -1) {
                zip.write(bytes, 0, read);
            }
            fileStream.close();
        }

        // creating manifest
        JSONObject manifest = new JSONObject();
        manifest.put("short_name", municipality.getShortName());
        manifest.put("name", municipality.getName());
        manifest.put("background_color", theme.getColour());
        manifest.put("theme_color", theme.getColour());
        manifest.put("start_url", theme.getStart_url());
        manifest.put("display", "standalone");
        manifest.put("icons",
                new JSONObject[]{
                        new JSONObject().put("src", themeId + "/icons-192.png").put("type", "image/png").put("sizes", "192x192"),
                        new JSONObject().put("src", themeId + "/icons-512.png").put("type", "image/png").put("sizes", "512x512")
                });
        writeJsonObject(zip, manifest, "manifest");

        // creating config
        JSONObject config = new JSONObject();
        config.put("fontfamily", theme.getFont_family());
        config.put("customerHomeUrl", theme.getHome_url());
        writeJsonObject(zip, config, "config");

        zip.closeEntry();
        zip.close();
        out.close();

        return Response.ok(out.toByteArray()).build();
    }
}
