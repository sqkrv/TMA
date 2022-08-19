package nl.utwente.teamepic.tomyappointment;

import nl.utwente.teamepic.tomyappointment.model.Municipality;
import nl.utwente.teamepic.tomyappointment.model.Theme;
import nl.utwente.teamepic.tomyappointment.resources.*;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/rest")
public class Application extends ResourceConfig {
    public Application() {
        super(
                Municipality.class, MunicipalityResource.class, MunicipalitiesResource.class,
                Theme.class, ThemeResource.class, ThemesResource.class,
                AllThemesResource.class,
                MultiPartFeature.class
        );
    }
}
