package nl.utwente.teamepic.tomyappointment.tests;

import nl.utwente.teamepic.tomyappointment.dao.MunicipalityDao;
import nl.utwente.teamepic.tomyappointment.dao.ThemeDao;
import nl.utwente.teamepic.tomyappointment.model.Municipality;
import nl.utwente.teamepic.tomyappointment.model.Theme;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ThemeDaoTest {
    ThemeDao dao;
    String municipalityId;
    String themeId;

    @BeforeAll
    void beforeAll() throws SQLException, ClassNotFoundException {
        MunicipalityDao municipalityDao = new MunicipalityDao();
        Municipality municipality = new Municipality("9a9b9c", "testing", "short testing");
        municipalityId = municipalityDao.insert(municipality);
        dao = new ThemeDao(municipalityId);
    }

    @AfterAll
    void afterAll() throws SQLException, ClassNotFoundException {
        new MunicipalityDao().delete(municipalityId);
    }

    @BeforeEach
    void setUp() throws SQLException {
        Theme theme = new Theme("d8656275-b3c6-4082-bfdb-d9d8be395d25", municipalityId, "location", "#ff1234", "https://google.com", "testfont.otf", "testfont", false);
        themeId = dao.insert(theme);
    }

    @AfterEach
    void tearDown() throws SQLException {
        dao.delete(themeId);
    }

    @Test
    void getAllOfAll() throws SQLException {
        List<Theme> themes = dao.getAllOfAll();
        for (Theme theme : themes) {
            if (theme.getID().equals(themeId) & theme.getMunicipality_id().equals(municipalityId))
                assertTrue(true);
        }
    }

    @Test
    void getAll() throws SQLException {
        Theme theme2 = new Theme("4cb818e5-c1ad-4c29-9da9-a82ab7b4b5de", municipalityId, "location", "#ff1234", "https://google.com", "testfont.otf", "testfont", false);
        String themeId2 = dao.insert(theme2);

        List<Theme> themes = dao.getAll();
        assertEquals(2, themes.size());
        for (Theme theme : themes) {
            assertTrue(theme.getID().equals(themeId) || theme.getID().equals(themeId2));
        }

        dao.delete(themeId2);
    }

    @Test
    void getById() throws SQLException {
        List<Theme> themes = dao.getById(themeId);

        assertEquals(themes.get(0).getID(), themeId);
    }

    @Test
    void insert() throws SQLException {
        List<Theme> themes = dao.getById(themeId);

        assertEquals(themes.get(0).getID(), themeId);
    }

    @Test
    void update() throws SQLException {
        List<Theme> themes = dao.getById(themeId);
        assertEquals(themes.get(0).getID(), themeId);
        assertEquals("location", themes.get(0).getLocation());

        Theme newTheme = new Theme(themeId, municipalityId, "new location", "#ff1234", "https://google.com", "testfont.otf", "testfont", false);
        dao.update(newTheme);
        themes = dao.getById(themeId);
        assertEquals(themes.get(0).getID(), themeId);
        assertEquals("new location", themes.get(0).getLocation());
    }

    @Test
    void delete() throws SQLException {
        Theme theme2 = new Theme("4d2f67ea-d7b6-4bec-a003-4d8f2cc45b4c", municipalityId, "location", "#ff1234", "https://google.com", "testfont.otf", "testfont", false);
        String themeId2 = dao.insert(theme2);

        List<Theme> themes = dao.getById(themeId2);
        assertEquals(themes.get(0).getID(), themeId2);

        dao.delete(themeId2);

        themes = dao.getById(themeId2);
        assertTrue(themes.isEmpty());
    }
}