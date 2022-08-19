package nl.utwente.teamepic.tomyappointment.resources;

import nl.utwente.teamepic.tomyappointment.model.Theme;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

import static nl.utwente.teamepic.tomyappointment.dao.ThemeDao.Columns.*;

public class FileHandler {
    private final Path initPath;
    private Theme theme;

    public FileHandler() {
//        this(Paths.get("/opt/tomcat/webapps/tomyappointment/resources"));  // deployment path
        this(Paths.get("C:\\Users\\hella\\Documents\\Module4\\Project\\teamepic-tma\\src\\main\\webapp\\resources"));  // dev path
    }

    public FileHandler(Path initPath, Theme theme) {
        this.initPath = initPath;
        this.theme = theme;
    }

    public FileHandler(Path initPath) {
        this.initPath = initPath;
    }

    public FileHandler(Theme theme) {
        this();
        this.theme = theme;
    }

    public boolean checkFile(FormDataContentDisposition fileDisposition) {
        if (fileDisposition.getFileName().isEmpty()) return false;

        String lowerFileName = fileDisposition.getFileName().toLowerCase();
        return lowerFileName.endsWith(".png") ||
                lowerFileName.endsWith(".jpg") ||
                lowerFileName.endsWith(".ttf") ||
                lowerFileName.endsWith(".otf");
    }

    public Path resolvePath() {
        Path path = initPath;
        path = path.resolve(theme.getMunicipality_id());
        path = path.resolve(theme.getID());

        return path;
    }

    public void handleFileUpload(InputStream file, FormDataContentDisposition fileDisposition) throws IOException {
        Path path = resolvePath();

        switch (fileDisposition.getName()) {
            case BACKGROUND:
                path = path.resolve("background.jpg");
                break;
            case LOGO:
                path = path.resolve("logo.png");
                break;
            case ICON_512:
                path = path.resolve("icon-512.png");
                break;
            case ICON_192:
                path = path.resolve("icon-192.png");
                break;
            case FONT_FILE:
                path = path.resolve(fileDisposition.getFileName());
                break;
        }
        writeToFile(file, path);
    }

    public void writeToFile(InputStream inputStream, Path filePath) throws IOException {
        File file = filePath.toFile();
        boolean fileExists = file.exists();
        if (!fileExists) fileExists = file.getParentFile().mkdirs();
        OutputStream out = new FileOutputStream(filePath.toFile());
        int read;
        byte[] bytes = new byte[1024];

        while ((read = inputStream.read(bytes)) != -1) {
            out.write(bytes, 0, read);
        }
        out.flush();
        out.close();
    }
}
